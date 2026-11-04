package io.github.pve123.techpoc.member;

import io.github.pve123.techpoc.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MemberApiTest extends IntegrationTest {

    @Test
    void 회원가입하면_비밀번호를_빼고_회원정보를_돌려준다() throws Exception {
        mockMvc.perform(post("/api/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "kim@test.com", "password": "password1234", "nickname": "철수"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("kim@test.com"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void 이미_가입된_이메일로는_가입할_수_없다() throws Exception {
        signUp("kim@test.com", "철수");

        mockMvc.perform(post("/api/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "kim@test.com", "password": "password1234", "nickname": "다른철수"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_USED"));
    }

    @Test
    void 이메일_형식이_틀리면_400으로_응답한다() throws Exception {
        mockMvc.perform(post("/api/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "not-an-email", "password": "password1234", "nickname": "철수"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 로그인하면_토큰으로_내_정보를_볼_수_있다() throws Exception {
        String token = signUpAndLogin("kim@test.com", "철수");

        mockMvc.perform(as(token, get("/api/v1/members/me")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("철수"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void 토큰_없이_내_정보를_보면_401로_응답한다() throws Exception {
        mockMvc.perform(get("/api/v1/members/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void 비밀번호가_틀리면_로그인할_수_없다() throws Exception {
        signUp("kim@test.com", "철수");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "kim@test.com", "password": "wrong-password"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("LOGIN_FAILED"));
    }

    @Test
    void 내_정보를_고칠_수_있다() throws Exception {
        String token = signUpAndLogin("kim@test.com", "철수");

        mockMvc.perform(as(token, patch("/api/v1/members/me"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nickname": "영희", "address": "서울", "birthDate": "1996-02-21", "phoneNumber": "010-0000-0000"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("영희"))
                .andExpect(jsonPath("$.birthDate").value("1996-02-21"));
    }

    @Test
    void 현재_비밀번호가_틀리면_비밀번호를_바꿀_수_없다() throws Exception {
        String token = signUpAndLogin("kim@test.com", "철수");

        mockMvc.perform(as(token, patch("/api/v1/members/me/password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currentPassword": "wrong-password", "newPassword": "new-password-1234"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("WRONG_PASSWORD"));
    }

    @Test
    void 비밀번호를_바꾸면_새_비밀번호로만_로그인된다() throws Exception {
        String token = signUpAndLogin("kim@test.com", "철수");

        mockMvc.perform(as(token, patch("/api/v1/members/me/password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currentPassword": "%s", "newPassword": "new-password-1234"}
                                """.formatted(PASSWORD)))
                .andExpect(status().isNoContent());

        login("kim@test.com", "new-password-1234");
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "kim@test.com", "password": "%s"}
                                """.formatted(PASSWORD)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 탈퇴하면_다시_로그인할_수_없다() throws Exception {
        String token = signUpAndLogin("kim@test.com", "철수");

        mockMvc.perform(as(token, delete("/api/v1/members/me")))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "kim@test.com", "password": "%s"}
                                """.formatted(PASSWORD)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 일반_회원은_관리자_API를_쓸_수_없다() throws Exception {
        String token = signUpAndLogin("kim@test.com", "철수");

        mockMvc.perform(as(token, get("/api/v1/admin/members")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void 관리자는_비밀번호_없이_일반_회원_목록을_본다() throws Exception {
        signUp("kim@test.com", "철수");
        signUp("lee@test.com", "영희");
        String adminToken = createAdminAndLogin();

        mockMvc.perform(as(adminToken, get("/api/v1/admin/members").param("page", "1").param("size", "10")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].email").value("lee@test.com"))
                .andExpect(jsonPath("$.content[0].password").doesNotExist());
    }
}

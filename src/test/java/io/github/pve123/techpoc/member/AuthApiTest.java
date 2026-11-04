package io.github.pve123.techpoc.member;

import io.github.pve123.techpoc.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthApiTest extends IntegrationTest {

    @Test
    void 리프레시_토큰으로_새_토큰을_받고_쓴_토큰은_다시_쓸_수_없다() throws Exception {
        signUp("kim@test.com", "철수");
        Tokens tokens = login("kim@test.com", PASSWORD);

        reissue(tokens.refreshToken())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());

        reissue(tokens.refreshToken())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_REFRESH_TOKEN"));
    }

    @Test
    void 액세스_토큰과_리프레시_토큰은_서로_바꿔_쓸_수_없다() throws Exception {
        signUp("kim@test.com", "철수");
        Tokens tokens = login("kim@test.com", PASSWORD);

        reissue(tokens.accessToken())
                .andExpect(status().isUnauthorized());

        mockMvc.perform(as(tokens.refreshToken(), get("/api/v1/members/me")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 로그아웃하면_리프레시_토큰을_쓸_수_없다() throws Exception {
        signUp("kim@test.com", "철수");
        Tokens tokens = login("kim@test.com", PASSWORD);

        mockMvc.perform(as(tokens.accessToken(), post("/api/v1/auth/logout")))
                .andExpect(status().isNoContent());

        reissue(tokens.refreshToken())
                .andExpect(status().isUnauthorized());
    }

    /** 예전 코드는 임시 비밀번호로 바꿀 때 조건이 없어서 모든 회원의 비밀번호가 바뀌었습니다. */
    @Test
    void 비밀번호를_재설정하면_그_회원의_비밀번호만_바뀐다() throws Exception {
        signUp("kim@test.com", "철수");
        signUp("lee@test.com", "영희");

        requestResetCode("kim@test.com");
        ArgumentCaptor<String> code = ArgumentCaptor.forClass(String.class);
        verify(mailSender).sendVerificationCode(eq("kim@test.com"), code.capture());

        mockMvc.perform(post("/api/v1/auth/password-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "kim@test.com", "code": "%s"}
                                """.formatted(code.getValue())))
                .andExpect(status().isNoContent());
        ArgumentCaptor<String> temporaryPassword = ArgumentCaptor.forClass(String.class);
        verify(mailSender).sendTemporaryPassword(eq("kim@test.com"), temporaryPassword.capture());

        login("kim@test.com", temporaryPassword.getValue());
        login("lee@test.com", PASSWORD);
    }

    @Test
    void 인증_코드가_틀리면_비밀번호를_재설정할_수_없다() throws Exception {
        signUp("kim@test.com", "철수");
        requestResetCode("kim@test.com");

        mockMvc.perform(post("/api/v1/auth/password-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "kim@test.com", "code": "000000x"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_VERIFICATION_CODE"));
    }

    @Test
    void 가입하지_않은_이메일도_같은_응답을_주지만_메일은_보내지_않는다() throws Exception {
        requestResetCode("nobody@test.com");

        verify(mailSender, never()).sendVerificationCode(anyString(), anyString());
    }

    private ResultActions reissue(String refreshToken) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/reissue")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"refreshToken": "%s"}
                        """.formatted(refreshToken)));
    }

    private void requestResetCode(String email) throws Exception {
        mockMvc.perform(post("/api/v1/auth/password-reset/code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s"}
                                """.formatted(email)))
                .andExpect(status().isNoContent());
    }
}

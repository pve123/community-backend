package io.github.pve123.techpoc.board;

import com.jayway.jsonpath.JsonPath;
import io.github.pve123.techpoc.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PostApiTest extends IntegrationTest {

    private String writerToken;
    private String otherToken;

    @BeforeEach
    void setUp() throws Exception {
        writerToken = signUpAndLogin("writer@test.com", "글쓴이");
        otherToken = signUpAndLogin("other@test.com", "구경꾼");
    }

    @Test
    void 글쓴이는_요청_본문이_아니라_토큰으로_정해진다() throws Exception {
        mockMvc.perform(as(writerToken, post("/api/v1/posts"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "제목", "content": "내용", "authorId": 999, "authorNickname": "가짜"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.authorNickname").value("글쓴이"))
                .andExpect(jsonPath("$.viewCount").value(0));
    }

    @Test
    void 다른_회원은_글을_고치거나_지울_수_없다() throws Exception {
        long postId = writePost(writerToken, "제목");

        mockMvc.perform(as(otherToken, patch("/api/v1/posts/{postId}", postId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "바꾼 제목", "content": "바꾼 내용"}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("NOT_POST_AUTHOR"));

        mockMvc.perform(as(otherToken, delete("/api/v1/posts/{postId}", postId)))
                .andExpect(status().isForbidden());
    }

    @Test
    void 글쓴이는_글을_고칠_수_있다() throws Exception {
        long postId = writePost(writerToken, "제목");

        mockMvc.perform(as(writerToken, patch("/api/v1/posts/{postId}", postId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "바꾼 제목", "content": "바꾼 내용"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("바꾼 제목"));
    }

    @Test
    void 같은_회원이_여러_번_봐도_조회수는_한_번만_오른다() throws Exception {
        long postId = writePost(writerToken, "제목");

        mockMvc.perform(as(otherToken, get("/api/v1/posts/{postId}", postId)))
                .andExpect(jsonPath("$.viewCount").value(1));
        mockMvc.perform(as(otherToken, get("/api/v1/posts/{postId}", postId)))
                .andExpect(jsonPath("$.viewCount").value(1));
        mockMvc.perform(as(writerToken, get("/api/v1/posts/{postId}", postId)))
                .andExpect(jsonPath("$.viewCount").value(2));
    }

    @Test
    void 없는_글을_보면_404로_응답한다() throws Exception {
        mockMvc.perform(as(writerToken, get("/api/v1/posts/{postId}", 9999)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("POST_NOT_FOUND"));
    }

    @Test
    void 글_목록은_최신_글부터_전체_개수와_함께_보여_준다() throws Exception {
        writePost(writerToken, "첫 번째");
        writePost(writerToken, "두 번째");
        writePost(otherToken, "세 번째");

        mockMvc.perform(as(writerToken, get("/api/v1/posts").param("page", "1").param("size", "2")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].title").value("세 번째"))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    /** 예전 코드는 조인 조건에 회원 ID 대신 게시글 ID를 넣어서 엉뚱한 글이 나왔습니다. */
    @Test
    void 회원이_쓴_글만_모아_볼_수_있다() throws Exception {
        writePost(writerToken, "글쓴이의 첫 글");
        writePost(writerToken, "글쓴이의 둘째 글");
        writePost(otherToken, "구경꾼의 글");
        long writerId = memberRepository.findByEmail("writer@test.com").orElseThrow().getId();

        mockMvc.perform(as(otherToken, get("/api/v1/members/{memberId}/posts", writerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].authorNickname").value("글쓴이"))
                .andExpect(jsonPath("$.content[1].authorNickname").value("글쓴이"));
    }

    @Test
    void 이미지를_올리고_지우면_파일도_함께_지워진다() throws Exception {
        long postId = writePost(writerToken, "제목");
        long filesBefore = countUploadedFiles();

        String body = mockMvc.perform(as(writerToken, multipart("/api/v1/posts/{postId}/images", postId)
                        .file(pngFile("cat.png"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.images[0].originalName").value("cat.png"))
                .andExpect(jsonPath("$.images[0].storedPath").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        long imageId = ((Number) JsonPath.read(body, "$.images[0].id")).longValue();
        assertThat(countUploadedFiles()).isEqualTo(filesBefore + 1);

        mockMvc.perform(as(writerToken, delete("/api/v1/posts/{postId}/images/{imageId}", postId, imageId)))
                .andExpect(status().isNoContent());
        assertThat(countUploadedFiles()).isEqualTo(filesBefore);
    }

    @Test
    void 이미지가_아닌_파일은_올릴_수_없다() throws Exception {
        long postId = writePost(writerToken, "제목");
        MockMultipartFile script = new MockMultipartFile("files", "run.sh", "text/plain", "echo hi".getBytes());

        mockMvc.perform(as(writerToken, multipart("/api/v1/posts/{postId}/images", postId).file(script)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("NOT_AN_IMAGE"));
    }

    @Test
    void 다른_회원은_이미지를_올리거나_지울_수_없다() throws Exception {
        long postId = writePost(writerToken, "제목");
        long filesBefore = countUploadedFiles();

        mockMvc.perform(as(otherToken, multipart("/api/v1/posts/{postId}/images", postId).file(pngFile("cat.png"))))
                .andExpect(status().isForbidden());
        assertThat(countUploadedFiles()).isEqualTo(filesBefore);
    }

    @Test
    void 글을_지우면_이미지_파일과_댓글도_함께_지워진다() throws Exception {
        long postId = writePost(writerToken, "제목");
        long filesBefore = countUploadedFiles();
        mockMvc.perform(as(writerToken, multipart("/api/v1/posts/{postId}/images", postId).file(pngFile("cat.png"))))
                .andExpect(status().isCreated());
        mockMvc.perform(as(otherToken, post("/api/v1/posts/{postId}/comments", postId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content": "좋은 글이네요"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(as(writerToken, delete("/api/v1/posts/{postId}", postId)))
                .andExpect(status().isNoContent());

        assertThat(countUploadedFiles()).isEqualTo(filesBefore);
        mockMvc.perform(as(writerToken, get("/api/v1/posts/{postId}/comments", postId)))
                .andExpect(status().isNotFound());
    }

    private long writePost(String token, String title) throws Exception {
        String body = mockMvc.perform(as(token, post("/api/v1/posts"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "%s", "content": "내용"}
                                """.formatted(title)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    private static MockMultipartFile pngFile(String name) {
        return new MockMultipartFile("files", name, MediaType.IMAGE_PNG_VALUE, new byte[]{(byte) 0x89, 'P', 'N', 'G'});
    }
}

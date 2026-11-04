package io.github.pve123.techpoc.board;

import com.jayway.jsonpath.JsonPath;
import io.github.pve123.techpoc.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CommentApiTest extends IntegrationTest {

    private String token;
    private long postId;

    @BeforeEach
    void setUp() throws Exception {
        token = signUpAndLogin("kim@test.com", "철수");
        postId = writePost();
    }

    @Test
    void 댓글_목록에는_댓글만_나오고_답글은_개수로_보여_준다() throws Exception {
        long commentId = idOf(writeComment(postId, "첫 댓글", null));
        writeComment(postId, "첫 답글", commentId);
        writeComment(postId, "둘째 답글", commentId);
        writeComment(postId, "둘째 댓글", null);

        mockMvc.perform(as(token, get("/api/v1/posts/{postId}/comments", postId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].content").value("첫 댓글"))
                .andExpect(jsonPath("$.content[0].replyCount").value(2))
                .andExpect(jsonPath("$.content[1].replyCount").value(0));

        mockMvc.perform(as(token, get("/api/v1/comments/{commentId}/replies", commentId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].content").value("첫 답글"))
                .andExpect(jsonPath("$.content[0].parentId").value(commentId))
                .andExpect(jsonPath("$.content[1].content").value("둘째 답글"));
    }

    @Test
    void 답글에는_답글을_달_수_없다() throws Exception {
        long commentId = idOf(writeComment(postId, "댓글", null));
        long replyId = idOf(writeComment(postId, "답글", commentId));

        writeComment(postId, "답글의 답글", replyId)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REPLY_TO_REPLY"));
    }

    @Test
    void 다른_글의_댓글에는_답글을_달_수_없다() throws Exception {
        long otherPostId = writePost();
        long commentOnOtherPost = idOf(writeComment(otherPostId, "다른 글의 댓글", null));

        writeComment(postId, "엉뚱한 답글", commentOnOtherPost)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PARENT_IN_OTHER_POST"));
    }

    private ResultActions writeComment(long targetPostId, String content, Long parentId) throws Exception {
        return mockMvc.perform(as(token, post("/api/v1/posts/{postId}/comments", targetPostId))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"content": "%s", "parentId": %s}
                        """.formatted(content, parentId)));
    }

    private long writePost() throws Exception {
        String body = mockMvc.perform(as(token, post("/api/v1/posts"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "제목", "content": "내용"}
                                """))
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    private static long idOf(ResultActions result) throws Exception {
        String body = result.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }
}

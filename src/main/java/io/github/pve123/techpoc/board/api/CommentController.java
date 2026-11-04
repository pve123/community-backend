package io.github.pve123.techpoc.board.api;

import io.github.pve123.techpoc.board.application.CommentService;
import io.github.pve123.techpoc.board.application.dto.CommentRequest;
import io.github.pve123.techpoc.board.application.dto.CommentResponse;
import io.github.pve123.techpoc.common.web.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "댓글")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "댓글·답글 쓰기", description = "parentId를 넣으면 그 댓글의 답글이 됩니다.")
    @PostMapping("/posts/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse write(@AuthenticationPrincipal Long memberId,
                                 @PathVariable Long postId,
                                 @Valid @RequestBody CommentRequest request) {
        return commentService.write(postId, memberId, request);
    }

    @Operation(summary = "댓글 목록", description = "오래된 댓글부터 보여 주고, 각 댓글의 답글 수를 함께 줍니다.")
    @GetMapping("/posts/{postId}/comments")
    public PageResponse<CommentResponse> getComments(@PathVariable Long postId,
                                                     @ParameterObject Pageable pageable) {
        return PageResponse.from(commentService.getComments(postId, pageable));
    }

    @Operation(summary = "답글 목록")
    @GetMapping("/comments/{commentId}/replies")
    public PageResponse<CommentResponse> getReplies(
            @PathVariable Long commentId,
            @ParameterObject @PageableDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return PageResponse.from(commentService.getReplies(commentId, pageable));
    }
}

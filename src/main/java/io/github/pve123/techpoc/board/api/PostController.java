package io.github.pve123.techpoc.board.api;

import io.github.pve123.techpoc.board.application.PostService;
import io.github.pve123.techpoc.board.application.dto.PostDetailResponse;
import io.github.pve123.techpoc.board.application.dto.PostRequest;
import io.github.pve123.techpoc.board.application.dto.PostSummaryResponse;
import io.github.pve123.techpoc.common.web.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "게시글")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class PostController {

    private final PostService postService;

    @Operation(summary = "글쓰기")
    @PostMapping("/posts")
    @ResponseStatus(HttpStatus.CREATED)
    public PostDetailResponse write(@AuthenticationPrincipal Long memberId,
                                    @Valid @RequestBody PostRequest request) {
        return postService.write(memberId, request);
    }

    @Operation(summary = "글 목록", description = "최신 글부터 보여 줍니다. page는 1부터 셉니다.")
    @GetMapping("/posts")
    public PageResponse<PostSummaryResponse> getPosts(@ParameterObject Pageable pageable) {
        return PageResponse.from(postService.getPosts(pageable));
    }

    @Operation(summary = "회원이 쓴 글 목록")
    @GetMapping("/members/{memberId}/posts")
    public PageResponse<PostSummaryResponse> getPostsByAuthor(@PathVariable Long memberId,
                                                              @ParameterObject Pageable pageable) {
        return PageResponse.from(postService.getPostsByAuthor(memberId, pageable));
    }

    @Operation(summary = "글 보기", description = "같은 회원이 하루에 여러 번 봐도 조회수는 한 번만 오릅니다.")
    @GetMapping("/posts/{postId}")
    public PostDetailResponse getPost(@AuthenticationPrincipal Long memberId, @PathVariable Long postId) {
        return postService.getPost(postId, memberId);
    }

    @Operation(summary = "글 고치기", description = "글쓴이만 고칠 수 있습니다.")
    @PatchMapping("/posts/{postId}")
    public PostDetailResponse edit(@AuthenticationPrincipal Long memberId,
                                   @PathVariable Long postId,
                                   @Valid @RequestBody PostRequest request) {
        return postService.edit(postId, memberId, request);
    }

    @Operation(summary = "글 지우기", description = "글쓴이만 지울 수 있습니다. 댓글과 이미지도 함께 지워집니다.")
    @DeleteMapping("/posts/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Long memberId, @PathVariable Long postId) {
        postService.delete(postId, memberId);
    }

    @Operation(summary = "이미지 올리기", description = "jpg, jpeg, png, gif, webp만 올릴 수 있습니다.")
    @PostMapping(path = "/posts/{postId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public PostDetailResponse addImages(@AuthenticationPrincipal Long memberId,
                                        @PathVariable Long postId,
                                        @RequestPart("files") List<MultipartFile> files) {
        return postService.addImages(postId, memberId, files);
    }

    @Operation(summary = "이미지 지우기")
    @DeleteMapping("/posts/{postId}/images/{imageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteImage(@AuthenticationPrincipal Long memberId,
                            @PathVariable Long postId,
                            @PathVariable Long imageId) {
        postService.deleteImage(postId, imageId, memberId);
    }
}

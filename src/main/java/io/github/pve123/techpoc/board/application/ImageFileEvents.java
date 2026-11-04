package io.github.pve123.techpoc.board.application;

/**
 * 이미지 파일과 DB가 어긋나지 않도록, 파일 삭제는 트랜잭션 결과를 본 뒤에 합니다.
 * <ul>
 *   <li>{@link Stored}: 파일을 저장했는데 트랜잭션이 실패하면 그 파일을 지웁니다.</li>
 *   <li>{@link Discarded}: DB에서 이미지를 지운 트랜잭션이 성공하면 파일도 지웁니다.</li>
 * </ul>
 */
public final class ImageFileEvents {

    private ImageFileEvents() {
    }

    public record Stored(String storedPath) {
    }

    public record Discarded(String storedPath) {
    }
}

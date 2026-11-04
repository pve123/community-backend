package io.github.pve123.techpoc.board.domain;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    @EntityGraph(attributePaths = "images")
    Optional<Post> findWithImagesById(Long id);

    /** 읽고-더하고-쓰는 대신 DB에서 한 번에 더해서, 동시에 조회해도 숫자가 빠지지 않게 합니다. */
    @Modifying(clearAutomatically = true)
    @Query("update Post p set p.viewCount = p.viewCount + 1 where p.id = :postId")
    void increaseViewCount(Long postId);
}

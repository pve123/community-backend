-- 회원 컨텍스트
CREATE TABLE member
(
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    email        VARCHAR(100) NOT NULL,
    password     VARCHAR(255) NOT NULL,
    nickname     VARCHAR(20)  NOT NULL,
    address      VARCHAR(255) NULL,
    birth_date   DATE         NULL,
    phone_number VARCHAR(20)  NULL,
    role         VARCHAR(20)  NOT NULL,
    created_at   DATETIME(6)  NOT NULL,
    updated_at   DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_member_email UNIQUE (email)
);

CREATE TABLE withdrawn_member
(
    member_id    BIGINT      NOT NULL,
    withdrawn_at DATETIME(6) NOT NULL,
    PRIMARY KEY (member_id)
);

-- 게시판 컨텍스트
-- 다른 컨텍스트(member)와는 외래 키를 걸지 않습니다. 회원이 탈퇴해도 글은 남습니다.
CREATE TABLE post
(
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    author_id       BIGINT       NOT NULL,
    author_nickname VARCHAR(20)  NOT NULL,
    title           VARCHAR(100) NOT NULL,
    content         TEXT         NOT NULL,
    view_count      BIGINT       NOT NULL DEFAULT 0,
    created_at      DATETIME(6)  NOT NULL,
    updated_at      DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_post_author_id (author_id)
);

CREATE TABLE post_image
(
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    post_id       BIGINT       NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    stored_path   VARCHAR(500) NOT NULL,
    created_at    DATETIME(6)  NOT NULL,
    updated_at    DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_post_image_post FOREIGN KEY (post_id) REFERENCES post (id) ON DELETE CASCADE
);

CREATE TABLE comment
(
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    post_id         BIGINT       NOT NULL,
    parent_id       BIGINT       NULL,
    author_id       BIGINT       NOT NULL,
    author_nickname VARCHAR(20)  NOT NULL,
    content         VARCHAR(500) NOT NULL,
    created_at      DATETIME(6)  NOT NULL,
    updated_at      DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_comment_post_parent (post_id, parent_id),
    INDEX idx_comment_parent (parent_id),
    CONSTRAINT fk_comment_post FOREIGN KEY (post_id) REFERENCES post (id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_parent FOREIGN KEY (parent_id) REFERENCES comment (id) ON DELETE CASCADE
);

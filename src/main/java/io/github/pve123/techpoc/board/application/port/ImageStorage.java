package io.github.pve123.techpoc.board.application.port;

import org.springframework.web.multipart.MultipartFile;

public interface ImageStorage {

    /** 이미지가 아니면 저장하지 않고 예외를 던집니다. */
    StoredImage store(MultipartFile file);

    void delete(String storedPath);

    record StoredImage(String originalName, String storedPath) {
    }
}

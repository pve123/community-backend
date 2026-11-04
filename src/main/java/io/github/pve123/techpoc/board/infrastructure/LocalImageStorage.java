package io.github.pve123.techpoc.board.infrastructure;

import io.github.pve123.techpoc.board.application.port.ImageStorage;
import io.github.pve123.techpoc.common.error.BusinessException;
import io.github.pve123.techpoc.common.error.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * 이미지를 서버 디스크에 저장합니다.
 *
 * <p>파일 이름은 사용자가 보낸 이름을 쓰지 않고 UUID로 새로 만듭니다.
 * DB에는 저장 폴더 기준 상대 경로만 남기고, 지울 때는 그 경로가 저장 폴더 안인지 다시 확인합니다.
 */
@Slf4j
@Component
class LocalImageStorage implements ImageStorage {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final DateTimeFormatter FOLDER_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final Path rootDirectory;

    LocalImageStorage(@Value("${app.upload.directory}") String rootDirectory) {
        this.rootDirectory = Path.of(rootDirectory).toAbsolutePath().normalize();
    }

    @Override
    public StoredImage store(MultipartFile file) {
        String originalName = StringUtils.getFilename(StringUtils.cleanPath(String.valueOf(file.getOriginalFilename())));
        String extension = String.valueOf(StringUtils.getFilenameExtension(originalName)).toLowerCase(Locale.ROOT);
        if (!isImage(file.getContentType(), extension)) {
            throw new BusinessException(ErrorCode.NOT_AN_IMAGE);
        }

        Path target = rootDirectory
                .resolve(LocalDate.now().format(FOLDER_FORMAT))
                .resolve(UUID.randomUUID() + "." + extension);
        try {
            Files.createDirectories(target.getParent());
            file.transferTo(target);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.IMAGE_SAVE_FAILED, e);
        }
        return new StoredImage(originalName, rootDirectory.relativize(target).toString());
    }

    @Override
    public void delete(String storedPath) {
        Path target = rootDirectory.resolve(storedPath).normalize();
        if (!target.startsWith(rootDirectory)) {
            log.warn("저장 폴더 밖의 파일은 지우지 않습니다: {}", storedPath);
            return;
        }
        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            log.warn("이미지 파일을 지우지 못했습니다: {}", storedPath, e);
        }
    }

    private static boolean isImage(String contentType, String extension) {
        return contentType != null
                && contentType.startsWith("image/")
                && ALLOWED_EXTENSIONS.contains(extension);
    }
}

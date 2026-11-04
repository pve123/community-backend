package io.github.pve123.techpoc.board.application;

import io.github.pve123.techpoc.board.application.port.ImageStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
class ImageFileCleaner {

    private final ImageStorage imageStorage;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    void deleteWhenSaveFailed(ImageFileEvents.Stored event) {
        imageStorage.delete(event.storedPath());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void deleteWhenDiscarded(ImageFileEvents.Discarded event) {
        imageStorage.delete(event.storedPath());
    }
}

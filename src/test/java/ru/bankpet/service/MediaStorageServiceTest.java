package ru.bankpet.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import ru.bankpet.entity.MediaAsset;
import ru.bankpet.repository.MediaAssetRepository;
import java.nio.file.Path;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MediaStorageServiceTest {
    @TempDir Path tempDir;

    @Test
    void encryptsAndDecryptsPdfWithoutChangingContent() throws Exception {
        MediaAssetRepository repository = mock(MediaAssetRepository.class);
        VideoCompressionService video = mock(VideoCompressionService.class);
        when(repository.sumStoredSizeByOwnerId("user-1")).thenReturn(0L);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        MediaStorageService service = new MediaStorageService(repository, video, tempDir.toString(), "a-production-length-test-key-that-is-secret", 10_000_000);
        service.initialize();
        byte[] content = "%PDF-1.7\nRentFlow test receipt".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        MediaAsset asset = service.store("user-1", "receipt", new MockMultipartFile("file", "receipt.pdf", "application/pdf", content));
        when(repository.findByIdAndOwnerId(asset.getId(), "user-1")).thenReturn(Optional.of(asset));

        assertArrayEquals(content, service.open("user-1", asset.getId()).readAllBytes());
        assertNotEquals(0, asset.getStoredSize());
        assertEquals(64, asset.getSha256().length());
    }

    @Test
    void rejectsFileWhoseSignatureDoesNotMatchMimeType() throws Exception {
        MediaAssetRepository repository = mock(MediaAssetRepository.class);
        MediaStorageService service = new MediaStorageService(repository, mock(VideoCompressionService.class), tempDir.toString(), "a-production-length-test-key-that-is-secret", 10_000_000);
        service.initialize();
        MockMultipartFile fakePdf = new MockMultipartFile("file", "attack.pdf", "application/pdf", "not a pdf".getBytes());
        assertThrows(IllegalArgumentException.class, () -> service.store("user-1", "document", fakePdf));
        verify(repository, never()).save(any());
    }

    @Test
    void authenticatedMetadataDetectsTampering() throws Exception {
        MediaAssetRepository repository = mock(MediaAssetRepository.class);
        when(repository.sumStoredSizeByOwnerId("user-1")).thenReturn(0L);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        MediaStorageService service = new MediaStorageService(repository, mock(VideoCompressionService.class), tempDir.toString(), "a-production-length-test-key-that-is-secret", 10_000_000);
        service.initialize();
        MediaAsset asset = service.store("user-1", "receipt", new MockMultipartFile("file", "receipt.pdf", "application/pdf", "%PDF-1.7\ntest".getBytes()));
        asset.setKind("document");
        when(repository.findByIdAndOwnerId(asset.getId(), "user-1")).thenReturn(Optional.of(asset));

        assertThrows(java.io.IOException.class, () -> service.open("user-1", asset.getId()).readAllBytes());
    }
}

package ru.bankpet.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.bankpet.entity.MediaAsset;
import ru.bankpet.service.MediaStorageService;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.*;

@RestController
@RequestMapping("/api/media")
public class MediaController {
    private final MediaStorageService storage;
    private final byte[] apiToken;

    public MediaController(MediaStorageService storage, @Value("${app.media.api-token:}") String apiToken) {
        this.storage = storage; this.apiToken = apiToken.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> upload(@RequestHeader("X-RentFlow-Token") String token,
            @RequestParam(defaultValue="document") String kind, @RequestPart("file") MultipartFile file) throws Exception {
        authorize(token); MediaAsset asset = storage.store("demo-user", kind, file);
        return Map.of("id", asset.getId(), "name", asset.getOriginalName(), "type", asset.getMediaType(), "size", asset.getStoredSize(), "sha256", asset.getSha256());
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> list(@RequestHeader("X-RentFlow-Token") String token) {
        authorize(token); List<Map<String, Object>> assets = storage.list("demo-user").stream().map(a -> Map.<String,Object>of("id",a.getId(),"name",a.getOriginalName(),"kind",a.getKind(),"type",a.getMediaType(),"size",a.getOriginalSize(),"createdAt",a.getCreatedAt())).toList();
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(assets);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InputStreamResource> download(@RequestHeader("X-RentFlow-Token") String token, @PathVariable UUID id) throws Exception {
        authorize(token); MediaAsset asset = storage.find("demo-user", id);
        InputStream stream = storage.open("demo-user", id);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).contentType(MediaType.parseMediaType(asset.getMediaType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(asset.getOriginalName(), java.nio.charset.StandardCharsets.UTF_8).build().toString())
                .body(new InputStreamResource(stream));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestHeader("X-RentFlow-Token") String token, @PathVariable UUID id) throws Exception {
        authorize(token); storage.delete("demo-user", id);
    }

    private void authorize(String token) {
        if (apiToken.length < 32 || !MessageDigest.isEqual(apiToken, token.getBytes(java.nio.charset.StandardCharsets.UTF_8))) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> invalidUpload(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(Map.of("error", exception.getMessage()));
    }
}

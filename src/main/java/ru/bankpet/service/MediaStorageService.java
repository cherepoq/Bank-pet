package ru.bankpet.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.bankpet.entity.MediaAsset;
import ru.bankpet.repository.MediaAssetRepository;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;

@Service
public class MediaStorageService {
    private static final byte[] FILE_MAGIC = new byte[]{'R','F','V','2'};
    private static final Set<String> ALLOWED_TYPES = Set.of("application/pdf", "image/jpeg", "image/png", "image/webp", "video/mp4", "video/quicktime", "video/webm");
    private static final Set<String> ALLOWED_KINDS = Set.of("document", "receipt", "video");
    private final MediaAssetRepository repository;
    private final VideoCompressionService videoCompression;
    private final Path root;
    private final SecretKeySpec key;
    private final long ownerQuotaBytes;

    public MediaStorageService(MediaAssetRepository repository, VideoCompressionService videoCompression,
            @Value("${app.media.storage-path:./data/media}") String storagePath,
            @Value("${app.media.encryption-key:rentflow-local-development-key-change-me}") String encryptionKey,
            @Value("${app.media.owner-quota-bytes:2147483648}") long ownerQuotaBytes) {
        this.repository = repository;
        this.videoCompression = videoCompression;
        this.root = Paths.get(storagePath).toAbsolutePath().normalize();
        this.ownerQuotaBytes = ownerQuotaBytes;
        if (encryptionKey.length() < 32) throw new IllegalArgumentException("Media encryption key must contain at least 32 characters");
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(encryptionKey.getBytes(StandardCharsets.UTF_8));
            this.key = new SecretKeySpec(digest, "AES");
        } catch (Exception e) { throw new IllegalStateException("Cannot initialize media encryption", e); }
    }

    @PostConstruct
    void initialize() throws IOException { Files.createDirectories(root); }

    public MediaAsset store(String ownerId, String kind, MultipartFile file) throws Exception {
        if (file.isEmpty()) throw new IllegalArgumentException("Empty file");
        if (!ALLOWED_KINDS.contains(kind)) throw new IllegalArgumentException("Unsupported media kind");
        String mediaType = Optional.ofNullable(file.getContentType()).orElse("application/octet-stream").toLowerCase(Locale.ROOT);
        if (!ALLOWED_TYPES.contains(mediaType)) throw new IllegalArgumentException("Unsupported media type");
        validateSignature(file, mediaType);
        long limit = mediaType.startsWith("video/") ? 500L * 1024 * 1024 : 25L * 1024 * 1024;
        if (file.getSize() > limit) throw new IllegalArgumentException("File is too large");
        long used = repository.sumStoredSizeByOwnerId(ownerId);
        if (used + file.getSize() > ownerQuotaBytes) throw new IllegalArgumentException("Storage quota exceeded");

        Path compressedVideo = mediaType.startsWith("video/") ? videoCompression.compress(file).orElse(null) : null;
        if (compressedVideo != null) mediaType = "video/mp4";
        UUID id = UUID.randomUUID(); String storageKey = id + ".rfv"; Path target = root.resolve(storageKey);
        byte[] iv = new byte[12]; new java.security.SecureRandom().nextBytes(iv);
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding"); cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));
        cipher.updateAAD(aad(id, ownerId, kind));
        try (InputStream source = compressedVideo == null ? file.getInputStream() : Files.newInputStream(compressedVideo);
             InputStream input = new java.security.DigestInputStream(source, digest);
             OutputStream raw = Files.newOutputStream(target, StandardOpenOption.CREATE_NEW);
             DataOutputStream header = new DataOutputStream(raw)) {
            header.write(FILE_MAGIC); header.writeInt(iv.length); header.write(iv);
            try (CipherOutputStream encrypted = new CipherOutputStream(header, cipher)) { input.transferTo(encrypted); }
        } catch (Exception e) { Files.deleteIfExists(target); throw e; }
        finally { if (compressedVideo != null) Files.deleteIfExists(compressedVideo); }

        MediaAsset asset = new MediaAsset(); asset.setId(id); asset.setOwnerId(ownerId); asset.setKind(kind);
        asset.setOriginalName(safeName(file.getOriginalFilename())); asset.setMediaType(mediaType); asset.setStorageKey(storageKey);
        asset.setSha256(HexFormat.of().formatHex(digest.digest())); asset.setOriginalSize(file.getSize()); asset.setStoredSize(Files.size(target)); asset.setCreatedAt(Instant.now());
        try { return repository.save(asset); }
        catch (RuntimeException e) { Files.deleteIfExists(target); throw e; }
    }

    public List<MediaAsset> list(String ownerId) { return repository.findAllByOwnerIdOrderByCreatedAtDesc(ownerId); }
    public MediaAsset find(String ownerId, UUID id) { return repository.findByIdAndOwnerId(id, ownerId).orElseThrow(); }

    public InputStream open(String ownerId, UUID id) throws Exception {
        MediaAsset asset = repository.findByIdAndOwnerId(id, ownerId).orElseThrow();
        DataInputStream input = new DataInputStream(Files.newInputStream(root.resolve(asset.getStorageKey())));
        byte[] magic = input.readNBytes(FILE_MAGIC.length);
        if (!MessageDigest.isEqual(magic, FILE_MAGIC)) { input.close(); throw new IOException("Invalid encrypted file format"); }
        int ivLength = input.readInt(); if (ivLength != 12) { input.close(); throw new IOException("Invalid encrypted file"); }
        byte[] iv = input.readNBytes(ivLength); Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv)); cipher.updateAAD(aad(id, ownerId, asset.getKind()));
        return new CipherInputStream(input, cipher);
    }

    public void delete(String ownerId, UUID id) throws IOException {
        MediaAsset asset = repository.findByIdAndOwnerId(id, ownerId).orElseThrow();
        Files.deleteIfExists(root.resolve(asset.getStorageKey()));
        repository.delete(asset);
    }

    private String safeName(String name) {
        String result = Optional.ofNullable(name).orElse("file").replaceAll("[^a-zA-Zа-яА-Я0-9._ -]", "_");
        return result.length() > 160 ? result.substring(result.length() - 160) : result;
    }

    private void validateSignature(MultipartFile file, String mediaType) throws IOException {
        byte[] header;
        try (InputStream input = file.getInputStream()) { header = input.readNBytes(16); }
        boolean valid = switch (mediaType) {
            case "application/pdf" -> startsWith(header, new byte[]{0x25,0x50,0x44,0x46});
            case "image/jpeg" -> startsWith(header, new byte[]{(byte)0xff,(byte)0xd8,(byte)0xff});
            case "image/png" -> startsWith(header, new byte[]{(byte)0x89,0x50,0x4e,0x47,0x0d,0x0a,0x1a,0x0a});
            case "image/webp" -> header.length >= 12 && new String(header, 0, 4, StandardCharsets.US_ASCII).equals("RIFF") && new String(header, 8, 4, StandardCharsets.US_ASCII).equals("WEBP");
            case "video/mp4", "video/quicktime" -> header.length >= 12 && new String(header, 4, 4, StandardCharsets.US_ASCII).equals("ftyp");
            case "video/webm" -> startsWith(header, new byte[]{0x1a,0x45,(byte)0xdf,(byte)0xa3});
            default -> false;
        };
        if (!valid) throw new IllegalArgumentException("File content does not match declared media type");
    }

    private boolean startsWith(byte[] value, byte[] prefix) {
        if (value.length < prefix.length) return false;
        for (int i = 0; i < prefix.length; i++) if (value[i] != prefix[i]) return false;
        return true;
    }

    private byte[] aad(UUID id, String ownerId, String kind) {
        return (id + "\n" + ownerId + "\n" + kind).getBytes(StandardCharsets.UTF_8);
    }
}

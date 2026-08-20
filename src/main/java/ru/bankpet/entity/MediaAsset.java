package ru.bankpet.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "media_assets")
public class MediaAsset {
    @Id
    private UUID id;
    @Column(nullable = false) private String ownerId;
    @Column(nullable = false) private String originalName;
    @Column(nullable = false) private String mediaType;
    @Column(nullable = false) private String kind;
    @Column(nullable = false) private String storageKey;
    @Column(nullable = false, length = 64) private String sha256;
    @Column(nullable = false) private long originalSize;
    @Column(nullable = false) private long storedSize;
    @Column(nullable = false) private Instant createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }
    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }
    public String getKind() { return kind; }
    public void setKind(String kind) { this.kind = kind; }
    public String getStorageKey() { return storageKey; }
    public void setStorageKey(String storageKey) { this.storageKey = storageKey; }
    public String getSha256() { return sha256; }
    public void setSha256(String sha256) { this.sha256 = sha256; }
    public long getOriginalSize() { return originalSize; }
    public void setOriginalSize(long originalSize) { this.originalSize = originalSize; }
    public long getStoredSize() { return storedSize; }
    public void setStoredSize(long storedSize) { this.storedSize = storedSize; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

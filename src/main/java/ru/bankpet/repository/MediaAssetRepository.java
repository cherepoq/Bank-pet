package ru.bankpet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.bankpet.entity.MediaAsset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MediaAssetRepository extends JpaRepository<MediaAsset, UUID> {
    List<MediaAsset> findAllByOwnerIdOrderByCreatedAtDesc(String ownerId);
    Optional<MediaAsset> findByIdAndOwnerId(UUID id, String ownerId);

    @Query("select coalesce(sum(m.storedSize), 0) from MediaAsset m where m.ownerId = :ownerId")
    long sumStoredSizeByOwnerId(@Param("ownerId") String ownerId);
}

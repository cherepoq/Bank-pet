package ru.bankpet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.bankpet.entity.SpendingFilterSettings;

import java.util.Optional;
import java.util.UUID;

public interface SpendingFilterSettingsRepository extends JpaRepository<SpendingFilterSettings, UUID> {
    Optional<SpendingFilterSettings> findByClientId(UUID clientId);
}

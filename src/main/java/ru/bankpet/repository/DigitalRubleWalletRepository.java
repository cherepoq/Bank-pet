package ru.bankpet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.bankpet.entity.DigitalRubleWallet;

import java.util.UUID;

public interface DigitalRubleWalletRepository extends JpaRepository<DigitalRubleWallet, UUID> {
}

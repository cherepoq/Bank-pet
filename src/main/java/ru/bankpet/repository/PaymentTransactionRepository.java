package ru.bankpet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.bankpet.entity.PaymentTransaction;

import java.util.UUID;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, UUID> {
}

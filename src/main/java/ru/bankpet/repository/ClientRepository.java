package ru.bankpet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.bankpet.entity.Client;

import java.util.Optional;
import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {
    Optional<Client> findByPhone(String phone);
}

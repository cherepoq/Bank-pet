package ru.bankpet.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "cards")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String encryptedPan;

    @Column(nullable = false, unique = true)
    private String cardReference;

    @Column(nullable = false)
    private String cardType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    public UUID getId() { return id; }
    public String getEncryptedPan() { return encryptedPan; }
    public void setEncryptedPan(String encryptedPan) { this.encryptedPan = encryptedPan; }
    public String getCardReference() { return cardReference; }
    public void setCardReference(String cardReference) { this.cardReference = cardReference; }
    public String getCardType() { return cardType; }
    public void setCardType(String cardType) { this.cardType = cardType; }
    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }
}

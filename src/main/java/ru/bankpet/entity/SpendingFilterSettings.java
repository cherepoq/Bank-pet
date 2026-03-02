package ru.bankpet.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "spending_filter_settings")
public class SpendingFilterSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false, unique = true)
    private Client client;

    @Column(nullable = false)
    private boolean llmAgentEnabled;

    @Column(nullable = false)
    private boolean hardBlockEnabled;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal confirmationThreshold;

    @Column(nullable = false, length = 500)
    private String blockedCategoriesCsv;

    @Column(nullable = false, length = 500)
    private String riskyCategoriesCsv;

    public UUID getId() { return id; }
    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }
    public boolean isLlmAgentEnabled() { return llmAgentEnabled; }
    public void setLlmAgentEnabled(boolean llmAgentEnabled) { this.llmAgentEnabled = llmAgentEnabled; }
    public boolean isHardBlockEnabled() { return hardBlockEnabled; }
    public void setHardBlockEnabled(boolean hardBlockEnabled) { this.hardBlockEnabled = hardBlockEnabled; }
    public BigDecimal getConfirmationThreshold() { return confirmationThreshold; }
    public void setConfirmationThreshold(BigDecimal confirmationThreshold) { this.confirmationThreshold = confirmationThreshold; }
    public String getBlockedCategoriesCsv() { return blockedCategoriesCsv; }
    public void setBlockedCategoriesCsv(String blockedCategoriesCsv) { this.blockedCategoriesCsv = blockedCategoriesCsv; }
    public String getRiskyCategoriesCsv() { return riskyCategoriesCsv; }
    public void setRiskyCategoriesCsv(String riskyCategoriesCsv) { this.riskyCategoriesCsv = riskyCategoriesCsv; }
}

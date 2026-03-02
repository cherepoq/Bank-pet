package ru.bankpet.service;

import org.springframework.stereotype.Component;
import ru.bankpet.dto.ExternalTransactionDto;

import java.math.BigDecimal;
import java.util.List;

@Component
public class ExternalBankHistorySyncService {

    public List<ExternalTransactionDto> pullLatest() {
        return List.of(
                new ExternalTransactionDto("OZON: бытовая техника", new BigDecimal("-6490.00"), "OZON", "Tinkoff"),
                new ExternalTransactionDto("WB: одежда", new BigDecimal("-3290.00"), "WB", "Sber"),
                new ExternalTransactionDto("Вкусняшки: кофе и десерт", new BigDecimal("-790.00"), "ВКУСНЯШКИ", "Alfa")
        );
    }
}

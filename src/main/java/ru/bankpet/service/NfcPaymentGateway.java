package ru.bankpet.service;

import org.springframework.stereotype.Component;
import ru.bankpet.dto.NfcPurchaseRequestDto;

@Component
public class NfcPaymentGateway {

    public NfcAuthorization authorize(NfcPurchaseRequestDto request) {
        if (request.deviceToken() == null || request.deviceToken().isBlank()) {
            return new NfcAuthorization(false, "NFC-токен устройства не передан. Оплата отклонена.");
        }
        return new NfcAuthorization(true, "NFC авторизация успешна");
    }

    public record NfcAuthorization(boolean approved, String message) {
    }
}

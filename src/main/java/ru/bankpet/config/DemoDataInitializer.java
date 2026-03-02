package ru.bankpet.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.bankpet.entity.*;
import ru.bankpet.repository.ClientRepository;
import ru.bankpet.repository.PaymentTransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Configuration
public class DemoDataInitializer {

    @Bean
    CommandLineRunner loadDemoData(ClientRepository clientRepository,
                                   PaymentTransactionRepository transactionRepository) {
        return args -> {
            if (clientRepository.count() > 0) {
                return;
            }

            Client client = new Client();
            client.setFullName("Иван Петров");
            client.setPhone("+7-900-123-45-67");

            Account account = new Account();
            account.setClient(client);
            account.setBalance(new BigDecimal("250000.00"));
            account.setIban("RU1200001111222233334444");
            client.getAccounts().add(account);

            Card debit = new Card();
            debit.setAccount(account);
            debit.setCardType("DEBIT");
            debit.setMaskedPan("2202 **** **** 4321");
            account.getCards().add(debit);

            Card credit = new Card();
            credit.setAccount(account);
            credit.setCardType("CREDIT");
            credit.setMaskedPan("2200 **** **** 6789");
            account.getCards().add(credit);

            DigitalRubleWallet wallet = new DigitalRubleWallet();
            wallet.setClient(client);
            wallet.setLinked(false);
            wallet.setBalance(new BigDecimal("1000.00"));
            client.setDigitalRubleWallet(wallet);

            clientRepository.save(client);

            PaymentTransaction trx1 = new PaymentTransaction();
            trx1.setClient(client);
            trx1.setTitle("Покупка: Озон");
            trx1.setAmount(new BigDecimal("-3590.00"));
            trx1.setSourceType("CARD");
            trx1.setCreatedAt(LocalDateTime.now().minusDays(1));

            PaymentTransaction trx2 = new PaymentTransaction();
            trx2.setClient(client);
            trx2.setTitle("Зарплата");
            trx2.setAmount(new BigDecimal("120000.00"));
            trx2.setSourceType("RUB_ACCOUNT");
            trx2.setCreatedAt(LocalDateTime.now().minusDays(3));

            transactionRepository.save(trx1);
            transactionRepository.save(trx2);
        };
    }
}

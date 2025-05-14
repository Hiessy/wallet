package org.cyan.core.event;

import lombok.extern.slf4j.Slf4j;
import org.cyan.core.data.AccountRepository;
import org.cyan.core.data.model.Account;
import org.cyan.core.event.model.AccountEvent;
import org.cyan.core.event.model.AliasCreatedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Slf4j
public class EventListener {

    private final AccountRepository accountRepository;
    private final KafkaTemplate<String, AccountEvent> kafkaTemplate;

    public EventListener(AccountRepository accountRepository, KafkaTemplate<String, AccountEvent> kafkaTemplate) {
        this.accountRepository = accountRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "${app.kafka.topics.alias-created}", groupId = "account-service-group")
    public void listenAliasCreatedEvent(AliasCreatedEvent event) {
        log.info("Received AliasCreatedEvent: {}", event);
        try {
            processAliasCreation(event);
        } catch (Exception e) {
            log.error("Error processing alias created event: {}", event, e);
            // Implement retry or dead-letter queue logic here

        }
    }
    @Transactional
    public void processAliasCreation(AliasCreatedEvent event) {
        log.info("Processing alias creation event: {}", event);

        // Check if account with this alias already exists
        if (accountRepository.findByAlias(event.getAlias()).isEmpty()) {
            Account newAccount = Account.builder()
                    .alias(event.getAlias())
                    .bankName("Default Bank") // Can be configured
                    .balance(BigDecimal.ZERO)
                    .build();

            Account savedAccount = accountRepository.save(newAccount);
            log.info("Created new account from alias event: {}", savedAccount);

            publishAccountEvent(savedAccount, "CREATED");
        }
    }

    public void publishAccountEvent(Account account, String eventType) {
        AccountEvent event = AccountEvent.builder()
                .accountId(account.getId())
                .eventType(eventType)
                .build();

        kafkaTemplate.send("account-events-topic", event);
        log.info("Published account event: {}", event);
    }
}


package org.cyan.core.event;

import org.cyan.core.data.AccountRepository;
import org.cyan.core.data.model.Account;
import org.cyan.core.event.model.AccountEvent;
import org.cyan.core.event.model.AliasCreatedEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class EventListenerTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private KafkaTemplate<String, AccountEvent> kafkaTemplate;

    @InjectMocks
    private EventListener eventListener;

    @Test
    void listenAliasCreatedEvent_WhenAliasDoesNotExist_ShouldCreateNewAccount() {
        // Arrange
        AliasCreatedEvent event = new AliasCreatedEvent(123L, "new-alias");
        Mockito.when(accountRepository.findByAlias(event.getAlias())).thenReturn(Optional.empty());
        Mockito.when(accountRepository.save(ArgumentMatchers.any(Account.class))).thenAnswer(invocation -> {
            Account a = invocation.getArgument(0);
            a.setId(1L);
            return a;
        });

        // Act
        eventListener.listenAliasCreatedEvent(event);

        // Assert
        Mockito.verify(accountRepository).findByAlias(event.getAlias());

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        Mockito.verify(accountRepository).save(accountCaptor.capture());

        Account savedAccount = accountCaptor.getValue();
        Assertions.assertEquals("new-alias", savedAccount.getAlias());
        Assertions.assertEquals("Default Bank", savedAccount.getBankName());
        Assertions.assertEquals(BigDecimal.ZERO, savedAccount.getBalance());

        Mockito.verify(kafkaTemplate).send(ArgumentMatchers.eq("account-events-topic"), ArgumentMatchers.any(AccountEvent.class));
    }

    @Test
    void listenAliasCreatedEvent_WhenAliasExists_ShouldNotCreateNewAccount() {
        // Arrange
        AliasCreatedEvent event = new AliasCreatedEvent(123L, "existing-alias");
        Account existingAccount = Account.builder().id(1L).alias("existing-alias").build();

        Mockito.when(accountRepository.findByAlias(event.getAlias())).thenReturn(Optional.of(existingAccount));

        // Act
        eventListener.listenAliasCreatedEvent(event);

        // Assert
        Mockito.verify(accountRepository).findByAlias(event.getAlias());
        Mockito.verify(accountRepository, Mockito.never()).save(ArgumentMatchers.any(Account.class));
        Mockito.verify(kafkaTemplate, Mockito.never()).send(ArgumentMatchers.eq("account-events-topic"), ArgumentMatchers.any(AccountEvent.class));
    }

    @Test
    void publishAccountEvent_ShouldSendEventToKafka() {
        // Arrange
        Account account = Account.builder()
                .id(1L)
                .alias("test-alias")
                .bankName("Test Bank")
                .balance(BigDecimal.valueOf(1000))
                .build();

        // Act
        eventListener.publishAccountEvent(account, "CREATED");

        // Assert
        ArgumentCaptor<AccountEvent> eventCaptor = ArgumentCaptor.forClass(AccountEvent.class);
        Mockito.verify(kafkaTemplate).send(ArgumentMatchers.eq("account-events-topic"), eventCaptor.capture());

        AccountEvent sentEvent = eventCaptor.getValue();
        Assertions.assertEquals(1L, sentEvent.getAccountId());
        Assertions.assertEquals("CREATED", sentEvent.getEventType());
    }

    @Test
    void processAliasCreation_ShouldCreateAccountAndPublishEvent() {
        // Arrange
        AliasCreatedEvent event = new AliasCreatedEvent(123L, "new-alias");
        Mockito.when(accountRepository.findByAlias(event.getAlias())).thenReturn(Optional.empty());
        Mockito.when(accountRepository.save(ArgumentMatchers.any(Account.class))).thenAnswer(invocation -> {
            Account a = invocation.getArgument(0);
            a.setId(1L);
            return a;
        });

        // Act
        eventListener.processAliasCreation(event);

        // Assert
        Mockito.verify(accountRepository).save(ArgumentMatchers.any(Account.class));
        Mockito.verify(kafkaTemplate).send(ArgumentMatchers.eq("account-events-topic"), ArgumentMatchers.any(AccountEvent.class));
    }
}
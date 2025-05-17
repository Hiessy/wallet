package org.cyan.core.service;

import org.cyan.core.data.AliasRepository;
import org.cyan.core.data.model.Alias;
import org.cyan.core.data.model.Account;
import org.cyan.exceptions.AliasNotFoundException;
import org.cyan.exceptions.InsufficientFundsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionServiceTest {

    @InjectMocks
    private TransactionService transactionService;

    @Mock
    private AliasRepository aliasRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private Alias createAlias(String name, double balance) {
        Alias alias = new Alias();
        alias.setName(name);

        Account account = new Account();
        account.setBalance(balance);
        account.setAlias(alias);

        alias.setAccount(account);
        return alias;
    }

    @Test
    void testTransferFundsSuccess() {
        Alias from = createAlias("Alice", 100.0);
        Alias to = createAlias("Bob", 50.0);

        when(aliasRepository.findByName("Alice")).thenReturn(from);
        when(aliasRepository.findByName("Bob")).thenReturn(to);

        transactionService.transferFunds("Alice", "Bob", 30.0);

        assertEquals(70.0, from.getAccount().getBalance());
        assertEquals(80.0, to.getAccount().getBalance());

        verify(aliasRepository).save(from);
        verify(aliasRepository).save(to);
        verify(kafkaTemplate).send(eq("transactions"), contains("Alice to Bob amount: 30.0"));
    }

    @Test
    void testTransferFundsFromAliasNotFound() {
        when(aliasRepository.findByName("Unknown")).thenReturn(null);

        AliasNotFoundException ex = assertThrows(AliasNotFoundException.class, () ->
                transactionService.transferFunds("Unknown", "Bob", 10.0)
        );

        assertTrue(ex.getMessage().contains("Alias 'Unknown' not found"));
    }

    @Test
    void testTransferFundsToAliasNotFound() {
        Alias from = createAlias("Alice", 100.0);
        when(aliasRepository.findByName("Alice")).thenReturn(from);
        when(aliasRepository.findByName("Unknown")).thenReturn(null);

        AliasNotFoundException ex = assertThrows(AliasNotFoundException.class, () ->
                transactionService.transferFunds("Alice", "Unknown", 10.0)
        );

        assertTrue(ex.getMessage().contains("Recipient alias 'Unknown' not found"));
    }

    @Test
    void testTransferFundsInsufficientBalance() {
        Alias from = createAlias("Alice", 10.0);
        Alias to = createAlias("Bob", 50.0);

        when(aliasRepository.findByName("Alice")).thenReturn(from);
        when(aliasRepository.findByName("Bob")).thenReturn(to);

        assertThrows(InsufficientFundsException.class, () ->
                transactionService.transferFunds("Alice", "Bob", 100.0)
        );
    }
}

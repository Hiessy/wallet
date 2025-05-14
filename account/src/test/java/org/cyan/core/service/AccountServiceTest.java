package org.cyan.core.service;

import org.cyan.core.data.AccountRepository;
import org.cyan.core.data.model.Account;
import org.cyan.core.event.EventListener;
import org.cyan.exception.AccountNotFoundException;
import org.cyan.in.model.AccountRequest;
import org.cyan.in.model.AccountResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private EventListener eventListener;

    @InjectMocks
    private AccountService accountService;

    @Test
    void createAccount_ShouldReturnCreatedAccount() {
        AccountRequest request = AccountRequest.builder().alias("test-alias").bankName("Test Bank").balance(BigDecimal.valueOf(1000)).build();
        Account savedAccount = Account.builder()
                .id(1L)
                .alias("test-alias")
                .bankName("Test Bank")
                .balance(BigDecimal.valueOf(1000))
                .build();

        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);

        // Act
        AccountResponse response = accountService.createAccount(request);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("test-alias", response.getAlias());
        assertEquals("Test Bank", response.getBankName());
        assertEquals(BigDecimal.valueOf(1000), response.getBalance());

        verify(accountRepository).save(any(Account.class));
        verify(eventListener).publishAccountEvent(savedAccount, "CREATED");
    }

    @Test
    void createAccount_WithNullBalance_ShouldSetZeroBalance() {
        AccountRequest request = AccountRequest.builder().alias("test-alias").bankName("Test Bank").balance(null).build();
        Account savedAccount = Account.builder()
                .id(1L)
                .alias("test-alias")
                .bankName("Test Bank")
                .balance(BigDecimal.ZERO)
                .build();

        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);

        // Act
        AccountResponse response = accountService.createAccount(request);

        // Assert
        assertEquals(BigDecimal.ZERO, response.getBalance());
    }

    @Test
    void getAccountById_WhenAccountExists_ShouldReturnAccount() throws AccountNotFoundException {
        // Arrange
        Long accountId = 1L;
        Account account = Account.builder()
                .id(accountId)
                .alias("test-alias")
                .bankName("Test Bank")
                .balance(BigDecimal.valueOf(1000))
                .build();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        // Act
        AccountResponse response = accountService.getAccountById(accountId);

        // Assert
        assertNotNull(response);
        assertEquals(accountId, response.getId());
        verify(accountRepository).findById(accountId);
    }

    @Test
    void getAccountById_WhenAccountNotExists_ShouldThrowException() {
        // Arrange
        Long accountId = 1L;
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AccountNotFoundException.class, () -> accountService.getAccountById(accountId));
    }

    @Test
    void updateAccount_WhenAccountExists_ShouldUpdateAndReturnAccount() throws AccountNotFoundException {
        // Arrange
        Long accountId = 1L;
        AccountRequest request = AccountRequest.builder().alias("updated-alias").bankName("Updated Bank").balance(BigDecimal.valueOf(2000)).build();

        Account existingAccount = Account.builder()
                .id(accountId)
                .alias("old-alias")
                .bankName("Old Bank")
                .balance(BigDecimal.valueOf(1000))
                .build();

        Account updatedAccount = Account.builder()
                .id(accountId)
                .alias("updated-alias")
                .bankName("Updated Bank")
                .balance(BigDecimal.valueOf(2000))
                .build();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(existingAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(updatedAccount);

        // Act
        AccountResponse response = accountService.updateAccount(accountId, request);

        // Assert
        assertNotNull(response);
        assertEquals(accountId, response.getId());
        assertEquals("updated-alias", response.getAlias());
        assertEquals("Updated Bank", response.getBankName());
        assertEquals(BigDecimal.valueOf(2000), response.getBalance());

        verify(accountRepository).findById(accountId);
        verify(accountRepository).save(existingAccount);
        verify(eventListener).publishAccountEvent(updatedAccount, "UPDATED");
    }

    @Test
    void updateAccount_WhenAccountNotExists_ShouldThrowException() {
        // Arrange
        Long accountId = 1L;
        AccountRequest request = AccountRequest.builder().alias("updated-alias").bankName("Updated Bank").balance(BigDecimal.valueOf(2000)).build();

        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AccountNotFoundException.class, () ->
                accountService.updateAccount(accountId, request));
    }

}
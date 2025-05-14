package org.cyan.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cyan.core.data.AccountRepository;
import org.cyan.core.data.model.Account;
import org.cyan.core.event.EventListener;
import org.cyan.exception.AccountNotFoundException;
import org.cyan.in.model.AccountRequest;
import org.cyan.in.model.AccountResponse;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {
    private final AccountRepository accountRepository;
    private final EventListener eventListener;

    @Transactional
    public AccountResponse createAccount(AccountRequest accountRequest) {
        Account account = Account.builder()
                .alias(accountRequest.getAlias())
                .bankName(accountRequest.getBankName())
                .balance(accountRequest.getBalance() != null ? accountRequest.getBalance() : BigDecimal.ZERO)
                .build();

        Account savedAccount = accountRepository.save(account);
        log.info("Account created with ID: {}", savedAccount.getId());

        eventListener.publishAccountEvent(savedAccount, "CREATED");

        return mapToDTO(savedAccount);
    }

    @Cacheable(value = "accounts", key = "#id")
    @Transactional(readOnly = true)
    public AccountResponse getAccountById(Long id) throws AccountNotFoundException {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with ID: " + id));
        return mapToDTO(account);
    }

    @CacheEvict(value = "accounts", key = "#id")
    @Transactional
    public AccountResponse updateAccount(Long id, AccountRequest accountRequest) throws AccountNotFoundException {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with ID: " + id));

        account.setBankName(accountRequest.getBankName());
        account.setBalance(accountRequest.getBalance());

        Account updatedAccount = accountRepository.save(account);
        log.info("Account updated with ID: {}", updatedAccount.getId());

        eventListener.publishAccountEvent(updatedAccount, "UPDATED");

        return mapToDTO(updatedAccount);
    }


    private AccountResponse mapToDTO(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .alias(account.getAlias())
                .bankName(account.getBankName())
                .balance(account.getBalance())
                .build();
    }

}

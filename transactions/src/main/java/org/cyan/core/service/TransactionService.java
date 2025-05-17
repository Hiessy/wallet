package org.cyan.core.service;

import org.cyan.core.data.AliasRepository;
import org.cyan.core.data.model.Alias;
import org.cyan.exceptions.AliasNotFoundException;
import org.cyan.exceptions.InsufficientFundsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

    @Autowired
    private AliasRepository aliasRepository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Transactional
    public void transferFunds(String fromAlias, String toAlias, double amount) {
        //TODO should return the account for this alias
        Alias fromUser = aliasRepository.findByName(fromAlias);
        if (fromUser == null) {
            throw new AliasNotFoundException("Alias '" + fromAlias + "' not found");
        }
        //TODO should return the account for this alias
        Alias toUser = aliasRepository.findByName(toAlias);
        if (toUser == null) {
            throw new AliasNotFoundException("Recipient alias '" + toAlias + "' not found");
        }

        if (fromUser.getAccount().getBalance() < amount) {
            throw new InsufficientFundsException("Insufficient funds");
        }

        //TODO create a new transaction and remove the balance from the account table
        fromUser.getAccount().setBalance(fromUser.getAccount().getBalance() - amount);
        toUser.getAccount().setBalance(toUser.getAccount().getBalance() + amount);

        aliasRepository.save(fromUser);
        aliasRepository.save(toUser);

        kafkaTemplate.send("transactions", "Transfer: " + fromAlias + " to " + toAlias + " amount: " + amount);
    }
}
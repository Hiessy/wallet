package org.cyan.core.data;

import org.cyan.core.data.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountRepository extends JpaRepository<Account, Long> {
    @Modifying
    @Query("UPDATE Account a SET a.balance = a.balance - :amount WHERE a.id = :accountId AND a.balance >= :amount")
    int updateBalance(@Param("accountId") Long accountId, @Param("amount") Double amount);

}

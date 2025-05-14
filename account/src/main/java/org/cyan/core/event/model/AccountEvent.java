package org.cyan.core.event.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cyan.core.data.model.Account;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountEvent {
    private Long accountId;
    private String eventType; // CREATED, UPDATED, DELETED
}
package org.cyan.in.model;

import lombok.Data;

@Data
public class TransferRequest {
    private String fromAlias;
    private String toAlias;
    private double amount;

}

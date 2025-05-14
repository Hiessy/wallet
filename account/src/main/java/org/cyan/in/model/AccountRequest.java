package org.cyan.in.model;

import jakarta.validation.GroupSequence;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import jakarta.validation.constraints.*;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@GroupSequence({ValidationGroups.First.class, ValidationGroups.Second.class, AccountRequest.class})
public class AccountRequest {

    @NotBlank(message = "Alias is required")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Alias must contain only letters and numbers")
    private String alias;

    @NotBlank(message = "Bank name is required", groups = ValidationGroups.First.class)
    @Size(min = 2, message = "Bank name must be at least 2 characters long", groups = ValidationGroups.Second.class)
    private String bankName;

    @NotNull(message = "Balance is required")
    @DecimalMin(value = "0.0", message = "Balance must be non-negative")
    private BigDecimal balance;
}

interface ValidationGroups {
    interface First {}
    interface Second {}
}
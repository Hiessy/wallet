package org.cyan.in.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class AccountRequest {

    @NotBlank(message = "Alias is required")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Alias must contain only letters and numbers")
    private String alias;

    @NotBlank(message = "Bank name is required")
    @Size(min = 2, message = "Bank name must be at least 2 characters long")
    private String bankName;

    @NotNull(message = "Balance is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Balance must be non-negative")
    private BigDecimal balance;
}

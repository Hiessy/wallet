package org.cyan.in;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import org.cyan.core.service.AccountService;
import org.cyan.exception.AccountNotFoundException;
import org.cyan.in.model.AccountRequest;
import org.cyan.in.model.AccountResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @Autowired
    private ObjectMapper objectMapper;

    private AccountRequest validRequest;
    private AccountResponse validResponse;

    @BeforeEach
    void setUp() {
        validRequest = AccountRequest.builder()
                .alias("validAlias")
                .bankName("Chase")
                .balance(new BigDecimal("100.00"))
                .build();

        validResponse = AccountResponse.builder()
                .id(1L)
                .alias("validAlias")
                .bankName("Chase")
                .balance(new BigDecimal("100.00"))
                .build();
    }

    @Test
    void shouldCreateAccountSuccessfully() throws Exception {
        Mockito.when(accountService.createAccount(Mockito.any())).thenReturn(validResponse);

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.alias").value("validAlias"))
                .andExpect(jsonPath("$.bankName").value("Chase"))
                .andExpect(jsonPath("$.balance").value(100.00));
    }

    @Test
    void shouldReturnBadRequestForInvalidAlias() throws Exception {
        AccountRequest invalid = validRequest.toBuilder().alias("bad*alias").build();

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Alias must contain only letters and numbers")));
    }

    @Test
    void shouldReturnBadRequestForEmptyBankName() throws Exception {
        AccountRequest invalid = validRequest.toBuilder().bankName("").build();

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Bank name is required")));
    }


    @Test
    void shouldValidateBankNameInOrder() {
        AccountRequest request = new AccountRequest("Alias123", "", BigDecimal.TEN);

        Set<ConstraintViolation<AccountRequest>> violations = Validation.buildDefaultValidatorFactory().getValidator().validate(request);

        // Should only contain NotBlank violation
        assertEquals(1, violations.size());
        assertEquals("Bank name is required", violations.iterator().next().getMessage());
    }

    @Test
    void shouldReturnBadRequestForNullBankName() throws Exception {
        AccountRequest invalid = validRequest.toBuilder().bankName(null).build();

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Bank name is required")));
    }

    @Test
    void shouldReturnBadRequestForNegativeBalance() throws Exception {
        AccountRequest invalid = validRequest.toBuilder().balance(new BigDecimal("-5.00")).build();

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Balance must be non-negative")));
    }

    @Test
    void shouldGetAccountSuccessfully() throws Exception {
        Mockito.when(accountService.getAccountById(1L)).thenReturn(validResponse);

        mockMvc.perform(get("/api/accounts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.alias").value("validAlias"));
    }

    @Test
    void shouldReturnNotFoundForMissingAccount() throws Exception {
        Mockito.when(accountService.getAccountById(999L))
                .thenThrow(new AccountNotFoundException("Account not found"));

        mockMvc.perform(get("/api/accounts/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Account not found")));
    }

    @Test
    void shouldUpdateAccountSuccessfully() throws Exception {
        Mockito.when(accountService.updateAccount(Mockito.eq(1L), Mockito.any()))
                .thenReturn(validResponse);

        mockMvc.perform(put("/api/accounts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alias").value("validAlias"));
    }

    @Test
    void shouldReturnBadRequestWhenUpdateFailsValidation() throws Exception {
        AccountRequest invalid = validRequest.toBuilder().alias("!bad").build();

        mockMvc.perform(put("/api/accounts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Alias must contain only letters and numbers")));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentAccount() throws Exception {
        Mockito.when(accountService.updateAccount(Mockito.eq(999L), Mockito.any()))
                .thenThrow(new AccountNotFoundException("Account not found"));

        mockMvc.perform(put("/api/accounts/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Account not found")));
    }
}

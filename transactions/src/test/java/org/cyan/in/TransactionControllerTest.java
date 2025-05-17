package org.cyan.in;

import org.cyan.core.service.TransactionService;
import org.cyan.exceptions.AliasNotFoundException;
import org.cyan.exceptions.InsufficientFundsException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class) // Replace with your controller class
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @Test
    void testTransferFunds_Success() throws Exception {
        // Configure mock behavior
        doNothing().when(transactionService).transferFunds(anyString(), anyString(), anyDouble());

        // JSON payload for the request body
        String jsonPayload = "{ \"fromAlias\": \"user1\", \"toAlias\": \"user2\", \"amount\": 200.0 }";

        // Execute and verify response
        mockMvc.perform(post("/api/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(content().string("Transfer successful"));

        // Verify method was called once
        Mockito.verify(transactionService).transferFunds("user1", "user2", 200.0);
    }

    @Test
    void testTransferFunds_Failure() throws Exception {
        // Configure mock behavior for insufficient funds scenario
        doThrow(new InsufficientFundsException("Insufficient funds")).when(transactionService)
                .transferFunds(anyString(), anyString(), anyDouble());

        // JSON payload for the request body
        String jsonPayload = "{ \"fromAlias\": \"user1\", \"toAlias\": \"user2\", \"amount\": 1500.0 }";

        // Execute and verify response
        mockMvc.perform(post("/api/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest());

        // Verify method was called once
        Mockito.verify(transactionService).transferFunds("user1", "user2", 1500.0);
    }

    @Test
    void testTransferFunds_AliasNotFound() throws Exception {
        // Mock the service to throw an AliasNotFoundException
        doThrow(new AliasNotFoundException("Alias 'alias1' not found"))
                .when(transactionService)
                .transferFunds("alias1", "alias2", 200.0);

        // JSON payload for the request body
        String jsonPayload = "{ \"fromAlias\": \"alias1\", \"toAlias\": \"alias2\", \"amount\": 200.0 }";

        // Perform the POST request
        mockMvc.perform(post("/api/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                // Validate the response status and body
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Alias not found"))
                .andExpect(jsonPath("$.message").value("Alias 'alias1' not found"));
    }
}
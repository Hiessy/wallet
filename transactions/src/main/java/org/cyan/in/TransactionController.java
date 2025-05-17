package org.cyan.in;

import org.cyan.core.service.TransactionService;
import org.cyan.in.model.TransferRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/transfer")
    public ResponseEntity<?> transferFunds(@RequestBody TransferRequest transferRequest) {
        transactionService.transferFunds(transferRequest.getFromAlias(), transferRequest.getToAlias(), transferRequest.getAmount());
        return ResponseEntity.ok("Transfer successful");
    }
}
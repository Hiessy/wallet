package org.cyan.in;

import jakarta.validation.Valid;
import org.cyan.core.service.AliasService;
import org.cyan.in.model.CreateAliasRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/alias")
public class AliasController {
    private final AliasService aliasService;

    public AliasController(AliasService aliasService) {
        this.aliasService = aliasService;
    }

    @PostMapping
    public ResponseEntity<?> createAlias(@RequestBody @Valid CreateAliasRequest request) {
        aliasService.createAlias(request);
        return ResponseEntity.noContent().build();
    }
}

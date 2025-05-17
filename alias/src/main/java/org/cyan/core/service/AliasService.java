package org.cyan.core.service;

import org.cyan.core.data.AliasRepository;
import org.cyan.core.data.model.Alias;
import org.cyan.core.event.KafkaProducerService;
import org.cyan.exceptions.DuplicateAliasException;
import org.cyan.in.model.CreateAliasRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AliasService {
    private final AliasRepository aliasRepository;
    private final KafkaProducerService kafkaProducerService;

    public AliasService(AliasRepository aliasRepository, KafkaProducerService kafkaProducerService) {
        this.aliasRepository = aliasRepository;
        this.kafkaProducerService = kafkaProducerService;
    }

    @Transactional
    public void createAlias(CreateAliasRequest request) {

        if (aliasRepository.existsByName(request.getName())) {
            throw new DuplicateAliasException("Alias name already exists");
        }

        Alias newAlias = Alias.builder().name(request.getName()).password(request.getPassword()).build();
        Alias savedAlias = aliasRepository.save(newAlias);

        // Publish event to Kafka
        kafkaProducerService.sendAliasRegisteredEvent(savedAlias);

    }

}

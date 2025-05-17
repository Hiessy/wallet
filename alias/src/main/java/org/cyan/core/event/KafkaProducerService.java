package org.cyan.core.event;

import org.cyan.core.data.model.Alias;
import org.cyan.core.event.model.AliasRegisteredEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, AliasRegisteredEvent> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, AliasRegisteredEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    public void sendAliasRegisteredEvent(Alias alias) {
        AliasRegisteredEvent event = AliasRegisteredEvent.builder().id(alias.getId()).name(alias.getName()).build();
        kafkaTemplate.send("alias-created-topic", event);
    }
}
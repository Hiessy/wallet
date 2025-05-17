package org.cyan.core.event;

import org.cyan.core.data.model.Alias;
import org.cyan.core.event.KafkaProducerService;
import org.cyan.core.event.model.AliasRegisteredEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Automatically initializes mocks
class KafkaProducerServiceTest {

    @Mock
    private KafkaTemplate<String, AliasRegisteredEvent> kafkaTemplate;

    @InjectMocks
    private KafkaProducerService kafkaProducerService;

    @Test
    void testSendAliasRegisteredEvent() {
        // Given
        Alias alias = Alias.builder().id(1L).name("JohnDoe123").build();

        // When
        kafkaProducerService.sendAliasRegisteredEvent(alias);

        // Then
        verify(kafkaTemplate, times(1)).send(eq("alias-created-topic"), any(AliasRegisteredEvent.class));
    }
}

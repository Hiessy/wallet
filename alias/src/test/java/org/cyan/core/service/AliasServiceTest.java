package org.cyan.core.service;

import org.cyan.core.data.AliasRepository;
import org.cyan.core.data.model.Alias;
import org.cyan.core.event.KafkaProducerService;
import org.cyan.exceptions.DuplicateAliasException;
import org.cyan.in.model.CreateAliasRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class AliasServiceTest {

    @Mock
    private AliasRepository aliasRepository;

    @Mock
    private KafkaProducerService kafkaProducerService;

    @InjectMocks
    private AliasService aliasService;

    @Test
    void testCreateAlias_Success() {
        CreateAliasRequest request = new CreateAliasRequest();
        request.setName("JohnDoe123");
        request.setPassword("password");

        // Mock repository behavior
        when(aliasRepository.existsByName(request.getName())).thenReturn(false);
        when(aliasRepository.save(any(Alias.class)))
                .thenAnswer(invocation -> {
                    Alias alias = invocation.getArgument(0);
                    alias.setId(1L);
                    return alias;
                });

        aliasService.createAlias(request);

        // Verify alias is saved and Kafka event is published
        verify(aliasRepository, times(1)).save(any(Alias.class));
        verify(kafkaProducerService, times(1)).sendAliasRegisteredEvent(any(Alias.class));
    }

    @Test
    void testCreateAlias_AliasExists() {
        CreateAliasRequest request = new CreateAliasRequest();
        request.setName("JohnDoe123");
        request.setPassword("password");

        // Mock alias already existing
        when(aliasRepository.existsByName(request.getName())).thenReturn(true);

        assertThrows(DuplicateAliasException.class, () -> aliasService.createAlias(request));

        // Ensure alias is NOT saved and Kafka event is NOT published
        verify(aliasRepository, never()).save(any(Alias.class));
        verify(kafkaProducerService, never()).sendAliasRegisteredEvent(any(Alias.class));
    }
}

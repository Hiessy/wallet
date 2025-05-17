package org.cyan.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cyan.core.data.AliasRepository;
import org.cyan.core.data.model.Alias;
import org.cyan.core.event.model.AliasRegisteredEvent;
import org.cyan.in.model.CreateAliasRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers // Simplify container management
class AliasIntegrationTest {

    @Container // Automatically start and stop the container
    private static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("banking_db")
                    .withUsername("postgres")
                    .withPassword("password");

    @Container // Automatically start and stop the container
    private static final KafkaContainer kafka =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"))
                    .withEnv("KAFKA_ADVERTISED_LISTENERS", "PLAINTEXT://localhost:9092");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Inject database properties
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        // Inject Kafka properties
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AliasRepository aliasRepository;

    @Autowired
    private KafkaTemplate<String, AliasRegisteredEvent> kafkaTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @AfterEach
    void tearDown() {
        // Clean up the database after each test
        aliasRepository.deleteAll();
    }

    @Test
    void shouldCreateAliasAndSendKafkaEvent() throws Exception {
        // Arrange
        CreateAliasRequest request = new CreateAliasRequest();
        request.setName("JohnDoe123");
        request.setPassword("password");

        // Act & Assert
        mockMvc.perform(post("/api/alias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        // Verify alias exists in the database
        assertTrue(aliasRepository.findByName("JohnDoe123").isPresent());
    }

    @Test
    void shouldNotAllowDuplicateAlias() throws Exception {
        // Arrange
        aliasRepository.save(Alias.builder().name("JohnDoe123").password("password").build());

        CreateAliasRequest request = new CreateAliasRequest();
        request.setName("JohnDoe123");
        request.setPassword("password");

        // Act & Assert
        mockMvc.perform(post("/api/alias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Alias name already exists"));
    }
}
package org.cyan.in;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cyan.core.service.AliasService;
import org.cyan.exceptions.DuplicateAliasException;
import org.cyan.in.model.CreateAliasRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AliasController.class) // Replace with your controller class
class AliasControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AliasService aliasService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldCreateAliasSuccessfully() throws Exception {
        CreateAliasRequest request = new CreateAliasRequest();
        request.setName("JohnDoe123");
        request.setPassword("password");

        doNothing().when(aliasService).createAlias(request);

        mockMvc.perform(post("/api/alias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldNotAllowDuplicateName() throws Exception {
        CreateAliasRequest request = new CreateAliasRequest();
        request.setName("ExistingAlias");
        request.setPassword("password");

        doThrow(new DuplicateAliasException("Alias already exists")).when(aliasService).createAlias(request);

        mockMvc.perform(post("/api/alias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Alias already exists"));
    }


    @Test
    void shouldNotAllowNullName() throws Exception {
        CreateAliasRequest request = new CreateAliasRequest();
        request.setName(null);
        request.setPassword("password");

        mockMvc.perform(post("/api/alias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Alias name cannot be null or blank"));
    }

    @Test
    void shouldNotAllowBlankName() throws Exception {
        CreateAliasRequest request = new CreateAliasRequest();
        request.setName("");
        request.setPassword("password");

        mockMvc.perform(post("/api/alias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Alias name cannot be null or blank"));
    }

    @Test
    void shouldNotAllowSpecialCharacters() throws Exception {
        CreateAliasRequest request = new CreateAliasRequest();
        request.setName("Invalid@Name!");
        request.setPassword("password");

        mockMvc.perform(post("/api/alias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Alias name can only contain letters and numbers"));
    }

    @Test
    void shouldNotAllowBlankPassword() throws Exception {
        CreateAliasRequest request = new CreateAliasRequest();
        request.setName("JohnDoe123");
        request.setPassword("");

        mockMvc.perform(post("/api/alias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Alias password cannot be null or blank"));
    }

    @Test
    void shouldNotAllowNullPassword() throws Exception {
        CreateAliasRequest request = new CreateAliasRequest();
        request.setName("JohnDoe123");
        request.setPassword(null);

        mockMvc.perform(post("/api/alias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Alias password cannot be null or blank"));
    }
}

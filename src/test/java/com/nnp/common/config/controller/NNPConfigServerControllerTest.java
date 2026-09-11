package com.nnp.common.config.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nnp.common.config.service.impl.NNPConfigOrchestratorService;
import com.nnp.common.config.to.AppMigrationResultTO;
import com.nnp.common.config.to.NNPConfigResponseTo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NNPConfigServerControllerTest {

    private MockMvc mockMvc;
    private NNPConfigOrchestratorService orchestratorService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        orchestratorService = Mockito.mock(NNPConfigOrchestratorService.class);
        NNPConfigServerController controller = new NNPConfigServerController(orchestratorService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testEncryptAppConfigs() throws Exception {
        AppMigrationResultTO mockResult = AppMigrationResultTO.builder()
                .application("redmine-int")
                .totalRecords(5)
                .processed(3)
                .skipped(2)
                .status("SUCCESS")
                .message("Encrypted 3 configurations")
                .build();

        when(orchestratorService.encryptApplicationConfigs("redmine-int")).thenReturn(mockResult);

        mockMvc.perform(post("/nnp-config/redmine-int/encrypt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.application").value("redmine-int"))
                .andExpect(jsonPath("$.processed").value(3))
                .andExpect(jsonPath("$.skipped").value(2))
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        // Test alternate path
        mockMvc.perform(post("/nnp-config/encrypt/redmine-int"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.application").value("redmine-int"));
    }

    @Test
    void testDecryptAppConfigs() throws Exception {
        AppMigrationResultTO mockResult = AppMigrationResultTO.builder()
                .application("redmine-int")
                .totalRecords(5)
                .processed(2)
                .skipped(3)
                .status("SUCCESS")
                .message("Decrypted 2 configurations")
                .build();

        when(orchestratorService.decryptApplicationConfigs("redmine-int")).thenReturn(mockResult);

        mockMvc.perform(post("/nnp-config/redmine-int/decrypt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.application").value("redmine-int"))
                .andExpect(jsonPath("$.processed").value(2))
                .andExpect(jsonPath("$.skipped").value(3))
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        // Test alternate path
        mockMvc.perform(post("/nnp-config/decrypt/redmine-int"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.application").value("redmine-int"));
    }

    @Test
    void testBulkConfig() throws Exception {
        NNPConfigResponseTo item = new NNPConfigResponseTo("app1", "dev", "latest", "K1", "V1", true);
        when(orchestratorService.bulkCreateOrUpdate(any())).thenReturn(List.of(item));

        mockMvc.perform(post("/nnp-config/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(item))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].key").value("K1"))
                .andExpect(jsonPath("$[0].is_encrypted").value(true));
    }
}

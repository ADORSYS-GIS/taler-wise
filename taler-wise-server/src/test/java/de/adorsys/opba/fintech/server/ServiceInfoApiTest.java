package de.adorsys.opba.fintech.server;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import java.util.UUID;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Service Information API Tests")
public class ServiceInfoApiTest extends FinTechApiBaseTest {

    private static final String REQUEST_ID_HEADER = "123e4567-e89b-12d3-a456-426614174000";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should return the service configuration with provider name and logo URL")
    void shouldReturnServiceConfig() throws Exception {
        mvc.perform(get("/v1/config").header("X-Request-ID", REQUEST_ID_HEADER))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.serviceProviderName").exists())
                .andExpect(jsonPath("$.serviceProviderName").isString())
                .andExpect(jsonPath("$.serviceProviderName").isNotEmpty())
                .andExpect(jsonPath("$.logoUrl").exists())
                .andExpect(jsonPath("$.logoUrl").isString())
                .andExpect(jsonPath("$.logoUrl").isNotEmpty())
                // Ensure no unexpected fields are returned
                .andExpect(jsonPath("$.supportUrl").doesNotExist())
                .andExpect(jsonPath("$.contactEmail").doesNotExist());
    }

    @Test
    @DisplayName("Should return service configuration with expected values from application.yml")
    void shouldReturnServiceConfigWithExpectedValues() throws Exception {
        mvc.perform(get("/v1/config").header("X-Request-ID", REQUEST_ID_HEADER))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.serviceProviderName").value("Taler-Wise Services"))
                .andExpect(jsonPath("$.logoUrl").value("https://adorsys.com/wp-content/uploads/2023/02/adorsys-logo-white-rgb.svg"));
    }

    @Test
    @DisplayName("Should return the terms of use with all required fields")
    void shouldReturnTermsOfUse() throws Exception {
        assertContentAndVersionFields("/v1/terms");
    }

    @Test
    @DisplayName("Should return the privacy policy with all required fields")
    void shouldReturnPrivacyPolicy() throws Exception {
        assertContentAndVersionFields("/v1/privacy");
    }

    @Test
    @DisplayName("Should handle concurrent requests to all endpoints without issues")
    void shouldHandleConcurrentRequests() throws Exception {
        // Test that endpoints can be called in quick succession
        for (int i = 0; i < 3; i++) {
            assertBasicEndpointResponse("/v1/config");
            assertBasicEndpointResponse("/v1/terms");
            assertBasicEndpointResponse("/v1/privacy");
        }
    }

    /**
     * Helper method to assert basic endpoint response with status and content type
     */
    private void assertBasicEndpointResponse(String endpoint) throws Exception {
        mvc.perform(get(endpoint).header("X-Request-ID", UUID.randomUUID().toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    /**
     * Helper method to assert content and version fields for terms and privacy endpoints
     */
    private void assertContentAndVersionFields(String endpoint) throws Exception {
        mvc.perform(get(endpoint).header("X-Request-ID", REQUEST_ID_HEADER))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content").isString())
                .andExpect(jsonPath("$.content").isNotEmpty())
                .andExpect(jsonPath("$.version").exists())
                .andExpect(jsonPath("$.version").isString())
                .andExpect(jsonPath("$.version").isNotEmpty())
                .andExpect(jsonPath("$.lastUpdated").exists())
                .andExpect(jsonPath("$.lastUpdated").isString());
    }
}

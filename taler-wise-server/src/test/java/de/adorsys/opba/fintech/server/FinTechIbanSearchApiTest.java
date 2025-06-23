package de.adorsys.opba.fintech.server;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class FinTechIbanSearchApiTest extends FinTechApiBaseTest {

    private static final String REQUEST_ID_HEADER = "123e4567-e89b-12d3-a456-426614174000";

    @Autowired
    private MockMvc mvc;

    @Test
    void ibanSearchBadRequest() throws Exception {
        String requestBody = "{ }";

        mvc.perform(post("/v1/search/bankInfo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Request-ID", REQUEST_ID_HEADER)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ibanSearchInvalidDigit() throws Exception {
        String requestBody = "{ \"iban\": \"DE00000000000000000000\" }";

        mvc.perform(post("/v1/search/bankInfo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Request-ID", REQUEST_ID_HEADER)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ibanSearchUnauthorized() throws Exception {
        String requestBody = "{ \"iban\": \"DE89370400440532013000\" }";

        mvc.perform(post("/v1/search/bankInfo")
                        .contentType(MediaType.APPLICATION_JSON)
                        // Simulate unauthorized by not providing a required auth header if one exists
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void ibanSearchMalformedIban() throws Exception {
        String requestBody = "{ \"iban\": \"foobar\" }";

        mvc.perform(post("/v1/search/bankInfo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Request-ID", REQUEST_ID_HEADER)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }
}

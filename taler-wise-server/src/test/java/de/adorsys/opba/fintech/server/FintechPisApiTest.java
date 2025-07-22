package de.adorsys.opba.fintech.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.opba.fintech.api.model.generated.OrchestratedSinglePaymentInitiationRequest;
import de.adorsys.opba.fintech.api.model.generated.SinglePaymentInitiationRequest;
import de.adorsys.opba.fintech.impl.config.EnableFinTechImplConfig;
import de.adorsys.opba.fintech.impl.controller.utils.RestRequestContext;
import de.adorsys.opba.fintech.impl.properties.CreditorProperties;
import de.adorsys.opba.fintech.impl.service.OrchestratedPaymentService;
import de.adorsys.opba.fintech.impl.service.PaymentService;
import de.adorsys.opba.fintech.server.config.TestConfig;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockReset;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TestConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EnableFinTechImplConfig
@AutoConfigureMockMvc
public class FintechPisApiTest extends FinTechApiBaseTest {

    private static final String FINTECH_PAYMENT_INITIATION_URL = "/v1/pis/banks/ {bankUUID}/accounts/ {accounId}/payments/single";
    private static final String FINTECH_ORCHESTRATED_PAYMENT_INITIATION_URL = "/v1/pis/banks/ {bankUUID}/accounts/ {accounId}/orchestrated/payments/single";

    @MockBean(reset = MockReset.NONE, answer = Answers.CALLS_REAL_METHODS)
    protected RestRequestContext restRequestContext;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private OrchestratedPaymentService orchestratedPaymentService;

    @MockBean
    private CreditorProperties creditorProperties;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);

    }

    @AfterEach
    public void finish() {
    }

    @Autowired
    protected MockMvc mvc;

   @Test
  public void SetCreditorPorperites (){
       CreditorProperties props = new CreditorProperties();
       props.setName("Demo Creditor");
       props.setIban("DE89370400440532013000");

       assertThat(props.getName()).isEqualTo("Demo Creditor");
       assertThat(props.getIban()).isEqualTo("DE89370400440532013000");
 }

    @Test
    @SneakyThrows
    public void shouldInitiatePaymentWithConfiguredCreditor() {
        // Given
        String bankUUID = "aa750320-2958-455e-9926-e9fca5ddfa92";
        String accountId = "DE38760700240320465700";
        UUID xRequestID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        String fintechRedirectURLOK = "https://fintech.com/ok";
        String fintechRedirectURLNOK = "https://fintech.com/notok";

        // Set creditor configuration
        when(creditorProperties.getName()).thenReturn("Configured Creditor");
        when(creditorProperties.getIban()).thenReturn("DE99999999999999999999");

        SinglePaymentInitiationRequest requestBody = new SinglePaymentInitiationRequest();
        requestBody.setAmount("200.00");
        requestBody.setDebitorIban("DE38760700240320465700");
        requestBody.setCreditorIban(creditorProperties.getIban());
        requestBody.setName(creditorProperties.getName());
        requestBody.setSubject("Test configured creditor");

        doReturn(ResponseEntity.accepted().build())
                .when(paymentService)
                .initiateSinglePayment(
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        anyBoolean(),
                        any(),
                        any(),
                        any()
                );
        // When
        var result = mvc.perform(post(FINTECH_PAYMENT_INITIATION_URL, bankUUID, accountId)
                        .header("X-Request-ID", xRequestID.toString())
                        .header("Fintech-Redirect-URL-OK", fintechRedirectURLOK)
                        .header("Fintech-Redirect-URL-NOK", fintechRedirectURLNOK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @SneakyThrows
    public void shouldInitiateOrchestratedPaymentWithConfiguredCreditor() {
        String bankUUID = "aa750320-2958-455e-9926-e9fca5ddfa92";
        String accountId = "DE38760700240320465700";
        UUID xRequestID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        String fintechRedirectURLOK = "https://fintech.com/ok";
        String fintechRedirectURLNOK = "https://fintech.com/notok";

        // Set creditor configuration
        when(creditorProperties.getName()).thenReturn("Configured Creditor");
        when(creditorProperties.getIban()).thenReturn("DE99999999999999999999");

        OrchestratedSinglePaymentInitiationRequest requestBody = new OrchestratedSinglePaymentInitiationRequest();
        requestBody.setAmount(BigDecimal.valueOf(200));
        requestBody.setDebitorIban("DE38760700240320465700");
        requestBody.setCreditorIban(creditorProperties.getIban());
        requestBody.setName(creditorProperties.getName());
        requestBody.setSubject("Test configured creditor");
        requestBody.setPsuId("max.musterman");

        doReturn(ResponseEntity.accepted().build())
                .when(orchestratedPaymentService)
                .initiateOrchestratedSinglePayment(
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        anyBoolean(),
                        any(),
                        any(),
                        any()
                );
        // When
        var result = mvc.perform(post(FINTECH_ORCHESTRATED_PAYMENT_INITIATION_URL, bankUUID, accountId)
                        .header("X-Request-ID", xRequestID.toString())
                        .header("Fintech-Redirect-URL-OK", fintechRedirectURLOK)
                        .header("Fintech-Redirect-URL-NOK", fintechRedirectURLNOK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    }






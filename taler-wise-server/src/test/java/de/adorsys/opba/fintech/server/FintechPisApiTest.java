package de.adorsys.opba.fintech.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.opba.fintech.api.model.generated.SinglePaymentInitiationRequest;
import de.adorsys.opba.fintech.impl.config.EnableFinTechImplConfig;
import de.adorsys.opba.fintech.impl.controller.utils.RestRequestContext;
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

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TestConfig.class)
@EnableFinTechImplConfig
@AutoConfigureMockMvc
public class FintechPisApiTest extends FinTechApiBaseTest {

    private static final String FINTECH_PAYMENT_INITIATION_URL = "/v1/pis/banks/ {bankUUID}/accounts/ {accounId}/payments/single";

    @MockBean(reset = MockReset.NONE, answer = Answers.CALLS_REAL_METHODS)
    protected RestRequestContext restRequestContext;

    @MockBean
    private PaymentService paymentService;

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
    @SneakyThrows
    public void paymentInitiation() {

        String bankUUID = "aa750320-2958-455e-9926-e9fca5ddfa92";
        String accountId = "DE38760700240320465700";
        UUID xRequestID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        String fintechRedirectURLOK = "https://fintech.com/ok";
        String fintechRedirectURLNOK = "https://fintech.com/notok";
        boolean xPisPsuAuthenticationRequired = true;
        boolean fintechDecoupledPreferred = false;
        String fintechBrandLoggingInformation = "FintechBrand";
        String fintechNotificationURI = "https://fintech.com/notify";
        String fintechRedirectNotificationContentPreferred = "PREFERRED";


        SinglePaymentInitiationRequest requestBody = new SinglePaymentInitiationRequest();
        requestBody.setAmount("100.00");
        requestBody.setDebitorIban("DE89370400440532013000");
        requestBody.setCreditorIban("DE89370400440532013000");
        requestBody.setPurpose("Payment for Order #123");
        requestBody.setName("string");
        requestBody.setInstantPayment(true);
        requestBody.setEndToEndIdentification("string");

        when(paymentService.initiateSinglePayment(
                eq(bankUUID),
                eq(accountId),
                any(SinglePaymentInitiationRequest.class),
                eq(fintechRedirectURLOK),
                eq(fintechRedirectURLNOK),
                eq(xPisPsuAuthenticationRequired),
                eq(fintechDecoupledPreferred),
                eq(fintechBrandLoggingInformation),
                eq(fintechNotificationURI),
                eq(fintechRedirectNotificationContentPreferred)
        )).thenReturn(ResponseEntity.ok().build());

        var result = mvc.perform(post(FINTECH_PAYMENT_INITIATION_URL, bankUUID, accountId)
                        .header("X-Request-ID", xRequestID.toString())
                        .header("Fintech-Redirect-URL-OK", fintechRedirectURLOK)
                        .header("Fintech-Redirect-URL-NOK", fintechRedirectURLNOK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }



}





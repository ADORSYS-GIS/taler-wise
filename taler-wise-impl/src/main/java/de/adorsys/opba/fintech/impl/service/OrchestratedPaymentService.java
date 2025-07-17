package de.adorsys.opba.fintech.impl.service;

import de.adorsys.opba.fintech.impl.tppclients.TppOrchestratedPaymentClient;
import de.adorsys.opba.fintech.api.model.generated.OrchestratedSinglePaymentInitiationRequest;
import de.adorsys.opba.fintech.impl.config.FintechUiConfig;
import de.adorsys.opba.fintech.impl.controller.utils.RestRequestContext;
import de.adorsys.opba.fintech.impl.database.entities.RedirectUrlsEntity;
import de.adorsys.opba.fintech.impl.database.repositories.PaymentRepository;
import de.adorsys.opba.fintech.impl.properties.CreditorProperties;
import de.adorsys.opba.fintech.impl.properties.TppProperties;
import de.adorsys.opba.fintech.impl.tppclients.ConsentType;
import de.adorsys.opba.tpp.pis.orchestrated.api.model.generated.PaymentInitiation;
import de.adorsys.opba.tpp.pis.orchestrated.api.model.generated.Amount;
import de.adorsys.opba.tpp.pis.orchestrated.api.model.generated.ConsentAuth;
import de.adorsys.opba.tpp.pis.orchestrated.api.model.generated.AccountReference;
import de.adorsys.opba.tpp.pis.orchestrated.api.model.generated.PsuAuthRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static de.adorsys.opba.fintech.impl.tppclients.Consts.COMPUTE_FINTECH_ID;
import static de.adorsys.opba.fintech.impl.tppclients.Consts.COMPUTE_X_REQUEST_SIGNATURE;
import static de.adorsys.opba.fintech.impl.tppclients.Consts.COMPUTE_X_TIMESTAMP_UTC;
import static de.adorsys.opba.fintech.impl.tppclients.Consts.HEADER_COMPUTE_PSU_IP_ADDRESS;

@Slf4j
@Service
@RequiredArgsConstructor

public class OrchestratedPaymentService {

    // FIXME: https://github.com/adorsys/open-banking-gateway/issues/316
    private static final String SEPA_PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String INSTANT_SEPA_PAYMENT_PRODUCT = "instant-sepa-credit-transfers";
    private static final String CURRENCY = "EUR";

    private final TppOrchestratedPaymentClient tppPisOrchestratedSinglePaymentClient;
    private final TppProperties tppProperties;
    private final RestRequestContext restRequestContext;
    private final FintechUiConfig uiConfig;
    private final RedirectHandlerService redirectHandlerService;
    private final HandleAcceptedService handleAcceptedService;
    private final PaymentRepository paymentRepository;
    private final CreditorProperties creditorProperties;

    public ResponseEntity<Void> initiateOrchestratedSinglePayment(String bankProfileId, String accountId,
                                                                  OrchestratedSinglePaymentInitiationRequest singlePaymentInitiationRequest,
                                                                  String fintechOkUrl, String fintechNOkUrl,
                                                                  Boolean fintechDecoupledPreferred,
                                                                  String fintechBrandLoggingInformation,
                                                                  String fintechNotificationURI,
                                                                  String fintechRedirectNotificationContentPreferred
    ) throws ExecutionException, InterruptedException {
        log.info("fill paramemeters for payment");
        final String fintechRedirectCode = UUID.randomUUID().toString();
        PsuAuthRequest psuAuthRequest = new PsuAuthRequest();
        psuAuthRequest.setExtras(Map.of("PSU_ID", singlePaymentInitiationRequest.getPsuId()));
        PaymentInitiation payment = new PaymentInitiation();
        payment.setCreditorAccount(getOrchestratedAccountReference(creditorProperties.getIban()));
        payment.setDebtorAccount(getOrchestratedAccountReference(singlePaymentInitiationRequest.getDebitorIban()));
        payment.setCreditorName(creditorProperties.getName());
        payment.setInstructedAmount(getOrchestratedAccountWithCurrency(singlePaymentInitiationRequest.getAmount()));
        payment.remittanceInformationUnstructured(singlePaymentInitiationRequest.getSubject());
        payment.setEndToEndIdentification(singlePaymentInitiationRequest.getEndToEndIdentification());
        payment.setPsuAuthRequest(psuAuthRequest);
        singlePaymentInitiationRequest.setInstantPayment(true);
        log.info("start call for orchestrated payment {} {}", fintechOkUrl, fintechNOkUrl);
        @SuppressWarnings("CPD-START")
        var paymentProduct = singlePaymentInitiationRequest.isInstantPayment() ? INSTANT_SEPA_PAYMENT_PRODUCT : SEPA_PAYMENT_PRODUCT;
        ResponseEntity<ConsentAuth> responseOfTpp = tppPisOrchestratedSinglePaymentClient.initiateOrchestratedPayment(
                payment.getRemittanceInformationUnstructured(),              //a public key need to be pass here
                RedirectUrlsEntity.buildPaymentOkUrl(uiConfig, fintechRedirectCode),
                RedirectUrlsEntity.buildPaymentNokUrl(uiConfig, fintechRedirectCode),
                UUID.fromString(restRequestContext.getRequestId()),
                paymentProduct,
                payment,
                COMPUTE_X_TIMESTAMP_UTC,
                COMPUTE_X_REQUEST_SIGNATURE,
                COMPUTE_FINTECH_ID,
                null,
                tppProperties.getFintechDataProtectionPassword(),
                UUID.fromString(bankProfileId),
                false,
                null, HEADER_COMPUTE_PSU_IP_ADDRESS, null,
                fintechDecoupledPreferred, fintechBrandLoggingInformation,
                fintechNotificationURI, fintechRedirectNotificationContentPreferred);
        if (responseOfTpp.getStatusCode() != HttpStatus.ACCEPTED) {
            throw new RuntimeException("Did expect status 202 from tpp, but got " + responseOfTpp.getStatusCodeValue());
        }
        redirectHandlerService.registerRedirectStateForSession(fintechRedirectCode, fintechOkUrl, fintechNOkUrl);
        return handleAcceptedService.handleAccepted(paymentRepository, ConsentType.PIS, bankProfileId, accountId, fintechRedirectCode,
                responseOfTpp.getHeaders(), paymentProduct);
    }


    private AccountReference getOrchestratedAccountReference(String iban) {
        AccountReference account = new AccountReference();
        account.setCurrency(CURRENCY);
        account.setIban(iban);
        return account;
    }

    private Amount getOrchestratedAccountWithCurrency(BigDecimal amountWithoutCurrency) {
        Amount amount = new Amount();
        amount.setCurrency(CURRENCY);
        amount.setAmount(amountWithoutCurrency);
        return amount;

    }

}

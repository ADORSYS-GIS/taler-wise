package de.adorsys.opba.fintech.impl.service;

import de.adorsys.opba.fintech.api.model.generated.PaymentInitiationWithStatusResponse;
import de.adorsys.opba.fintech.api.model.generated.SinglePaymentInitiationRequest;
import de.adorsys.opba.fintech.impl.config.FintechUiConfig;
import de.adorsys.opba.fintech.impl.controller.utils.RestRequestContext;
import de.adorsys.opba.fintech.impl.database.entities.PaymentEntity;
import de.adorsys.opba.fintech.impl.database.entities.RedirectUrlsEntity;
import de.adorsys.opba.fintech.impl.database.repositories.PaymentRepository;
import de.adorsys.opba.fintech.impl.mapper.PaymentInitiationWithStatusResponseMapper;
import de.adorsys.opba.fintech.impl.mapper.PaymentStatusResponseMapper;
import de.adorsys.opba.fintech.impl.properties.CreditorProperties;
import de.adorsys.opba.fintech.impl.properties.TppProperties;
import de.adorsys.opba.fintech.impl.tppclients.ConsentType;
import de.adorsys.opba.fintech.api.model.generated.PaymentStatusResponse;
import de.adorsys.opba.fintech.impl.tppclients.TppPisPaymentStatusClient;
import de.adorsys.opba.fintech.impl.tppclients.TppPisSinglePaymentClient;
import de.adorsys.opba.tpp.pis.api.model.generated.PaymentInitiation;
import de.adorsys.opba.tpp.pis.api.model.generated.PaymentInitiationResponse;
import de.adorsys.opba.tpp.pis.api.model.generated.AccountReference;
import de.adorsys.opba.tpp.pis.api.model.generated.Amount;
import de.adorsys.opba.tpp.pis.api.model.generated.PaymentInformationResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.Optional;

import static de.adorsys.opba.fintech.impl.tppclients.Consts.COMPUTE_FINTECH_ID;
import static de.adorsys.opba.fintech.impl.tppclients.Consts.COMPUTE_X_REQUEST_SIGNATURE;
import static de.adorsys.opba.fintech.impl.tppclients.Consts.COMPUTE_X_TIMESTAMP_UTC;
import static de.adorsys.opba.fintech.impl.tppclients.Consts.HEADER_COMPUTE_PSU_IP_ADDRESS;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    // FIXME: https://github.com/adorsys/open-banking-gateway/issues/316
    private static final String SEPA_PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String INSTANT_SEPA_PAYMENT_PRODUCT = "instant-sepa-credit-transfers";
    private static final String CURRENCY = "EUR";

    private static final Map<String, String> STATUS_MESSAGES = Map.ofEntries(
            Map.entry("ACCC", "Your payment has been fully settled and completed."),
            Map.entry("ACCP", "The payment initiation has been accepted based on your profile."),
            Map.entry("ACSC", "Your payment has been successfully completed."),
            Map.entry("ACSP", "Your payment has been accepted and is being processed."),
            Map.entry("ACTC", "The payment passed technical checks and is being processed."),
            Map.entry("ACWC", "Your payment was accepted but with some changes applied."),
            Map.entry("ACWP", "Your payment has been accepted but not yet posted to the account."),
            Map.entry("PDNG", "Your payment is pending. Please wait for confirmation."),
            Map.entry("RJCT", "Your payment was rejected. Please contact your bank or check details."),
            Map.entry("RCVD", "Your payment request has been received and is awaiting processing."),
            Map.entry("CANC", "Your payment request has been cancelled."),
            Map.entry("PART", "Your payment has been partially processed. Some parts were not executed.")
    );

    private final TppPisSinglePaymentClient tppPisSinglePaymentClient;
    private final TppPisPaymentStatusClient tppPisPaymentStatusClient;
    private final TppProperties tppProperties;
    private final RestRequestContext restRequestContext;
    private final FintechUiConfig uiConfig;
    private final RedirectHandlerService redirectHandlerService;
    private final HandleAcceptedService handleAcceptedService;
    private final PaymentRepository paymentRepository;
    private final CreditorProperties creditorProperties;

    public ResponseEntity<Void> initiateSinglePayment(String bankProfileId, String accountId, SinglePaymentInitiationRequest singlePaymentInitiationRequest,
                                                      String fintechOkUrl, String fintechNOkUrl, Boolean fintechDecoupledPreferred,
                                                      String fintechBrandLoggingInformation, String fintechNotificationURI, String fintechRedirectNotificationContentPreferred) {
        log.info("fill paramemeters for payment");
        final String fintechRedirectCode = UUID.randomUUID().toString();
        PaymentInitiation payment = new PaymentInitiation();
        payment.setCreditorAccount(getAccountReference(creditorProperties.getIban()));
        payment.setDebtorAccount(getAccountReference(singlePaymentInitiationRequest.getDebitorIban()));
        payment.setCreditorName(creditorProperties.getName());
        payment.setInstructedAmount(getAmountWithCurrency(singlePaymentInitiationRequest.getAmount()));
        payment.remittanceInformationUnstructured(singlePaymentInitiationRequest.getSubject());
        payment.setEndToEndIdentification(singlePaymentInitiationRequest.getEndToEndIdentification());
        singlePaymentInitiationRequest.setInstantPayment(true);
        log.info("start call for payment {} {}", fintechOkUrl, fintechNOkUrl);
        var paymentProduct = singlePaymentInitiationRequest.isInstantPayment() ? INSTANT_SEPA_PAYMENT_PRODUCT : SEPA_PAYMENT_PRODUCT;
        ResponseEntity<PaymentInitiationResponse> responseOfTpp = tppPisSinglePaymentClient.initiatePayment(
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
                null,
                HEADER_COMPUTE_PSU_IP_ADDRESS,
                null,
                fintechDecoupledPreferred, fintechBrandLoggingInformation,
                fintechNotificationURI, fintechRedirectNotificationContentPreferred);
        if (responseOfTpp.getStatusCode() != HttpStatus.ACCEPTED) {
            throw new RuntimeException("Did expect status 202 from tpp, but got " + responseOfTpp.getStatusCodeValue());
        }

        redirectHandlerService.registerRedirectStateForSession(fintechRedirectCode, fintechOkUrl, fintechNOkUrl);
        return handleAcceptedService.handleAccepted(paymentRepository, ConsentType.PIS, bankProfileId, accountId, fintechRedirectCode,
                responseOfTpp.getHeaders(), paymentProduct);
    }

    public ResponseEntity<List<PaymentInitiationWithStatusResponse>> retrieveAllSinglePayments(String bankProfileID, String accountId) {
        // TODO https://app.zenhub.com/workspaces/open-banking-gateway-5dd3b3daf010250001260675/issues/adorsys/open-banking-gateway/812
        // TODO https://app.zenhub.com/workspaces/open-banking-gateway-5dd3b3daf010250001260675/issues/adorsys/open-banking-gateway/794
        List<PaymentEntity> payments = paymentRepository.findByBankIdAndAccountIdAndPaymentConfirmed(bankProfileID, accountId, false);
        if (payments.isEmpty()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
        }
        List<PaymentInitiationWithStatusResponse> result = new ArrayList<>();

        for (PaymentEntity payment : payments) {
            PaymentInformationResponse body = tppPisPaymentStatusClient.getPaymentInformation(
                    UUID.fromString(restRequestContext.getRequestId()),
                    payment.getPaymentProduct(),
                    COMPUTE_X_TIMESTAMP_UTC,
                    COMPUTE_X_REQUEST_SIGNATURE,
                    COMPUTE_FINTECH_ID,
                    null,
                    tppProperties.getFintechDataProtectionPassword(),
                    UUID.fromString(bankProfileID),
                    payment.getTppServiceSessionId()).getBody();
            PaymentInitiationWithStatusResponse paymentInitiationWithStatusResponse = Mappers
                    .getMapper(PaymentInitiationWithStatusResponseMapper.class)
                    .mapFromTppToFintech(body);
            result.add(paymentInitiationWithStatusResponse);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    public ResponseEntity<PaymentStatusResponse> getPaymentStatus(String authId, String paymentProduct, String bankProfileID) {
        Optional<PaymentEntity> payment = paymentRepository.findByTppAuthId(authId);
        if (payment.isEmpty()) {
            return new ResponseEntity<>(null, HttpStatus.OK);

        }

            de.adorsys.opba.tpp.pis.api.model.generated.PaymentStatusResponse body = tppPisPaymentStatusClient.getPaymentStatus(
                    UUID.fromString(restRequestContext.getRequestId()),
                    paymentProduct,
                    COMPUTE_X_TIMESTAMP_UTC,
                    COMPUTE_X_REQUEST_SIGNATURE,
                    COMPUTE_FINTECH_ID,
                    null,
                    tppProperties.getFintechDataProtectionPassword(),
                    UUID.fromString(bankProfileID),
                    payment.get().getTppServiceSessionId()).getBody();

        PaymentStatusResponse paymentStatusResponse =  Mappers
                .getMapper(PaymentStatusResponseMapper.class)
                .mapFromTppToFintech(body);
        paymentStatusResponse.setPsuMessage(STATUS_MESSAGES.get(paymentStatusResponse.getTransactionStatus()));

        return new ResponseEntity<>(paymentStatusResponse, HttpStatus.OK);
    }
    private AccountReference getAccountReference(String iban) {
        AccountReference account = new AccountReference();
        account.setCurrency(CURRENCY);
        account.setIban(iban);
        return account;
    }

    private Amount getAmountWithCurrency(String amountWihthoutCurrency) {
        Amount amount = new Amount();
        amount.setCurrency(CURRENCY);
        amount.setAmount(amountWihthoutCurrency);
        return amount;
    }

}

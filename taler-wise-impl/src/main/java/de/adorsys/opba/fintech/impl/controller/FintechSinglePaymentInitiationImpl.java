package de.adorsys.opba.fintech.impl.controller;

import de.adorsys.opba.fintech.api.model.generated.SinglePaymentInitiationRequest;
import de.adorsys.opba.fintech.api.resource.generated.FintechSinglePaymentInitiationApi;
import de.adorsys.opba.fintech.impl.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class FintechSinglePaymentInitiationImpl implements FintechSinglePaymentInitiationApi {
    private final PaymentService paymentService;

    @Override
    public ResponseEntity<Void> initiateSinglePayment(
            UUID xRequestID,
            String fintechRedirectURLOK,
            String fintechRedirectURLNOK,
            String bankId,
            String accountId,
            SinglePaymentInitiationRequest body,
            Boolean xPisPsuAuthenticationRequired,
            Boolean fintechDecoupledPreferred,
            String fintechBrandLoggingInformation,
            String fintechNotificationURI,
            String fintechRedirectNotificationContentPreferred) {
        log.debug("got initiate payment request");

        return paymentService.initiateSinglePayment(bankId, accountId, body,
                        fintechRedirectURLOK, fintechRedirectURLNOK, xPisPsuAuthenticationRequired,
                        fintechDecoupledPreferred, fintechBrandLoggingInformation, fintechNotificationURI,
                        fintechRedirectNotificationContentPreferred);
    }
}

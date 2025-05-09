package de.adorsys.opba.fintech.impl.controller;

import de.adorsys.opba.fintech.api.model.generated.PaymentInitiationWithStatusResponse;
import de.adorsys.opba.fintech.api.resource.generated.FintechRetrieveAllSinglePaymentsApi;
import de.adorsys.opba.fintech.impl.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class FintechRetrieveAllSinglePaymentsImpl implements FintechRetrieveAllSinglePaymentsApi {
    private final PaymentService paymentService;

    @Override
    public ResponseEntity<List<PaymentInitiationWithStatusResponse>> retrieveAllSinglePayments(
            String bankId,
            String accountId,
            UUID xRequestID
    ) {
        log.debug("got list all payment request");


        return paymentService.retrieveAllSinglePayments(bankId, accountId);
    }
}

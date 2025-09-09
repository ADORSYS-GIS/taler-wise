package de.adorsys.opba.fintech.impl.controller;

import de.adorsys.opba.fintech.api.model.generated.PaymentStatusResponse;
import de.adorsys.opba.fintech.api.resource.generated.FintechGetPaymentStatusApi;
import de.adorsys.opba.fintech.impl.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;


@Slf4j
@RestController
@RequiredArgsConstructor
public class FintechGetPaymentStatus implements FintechGetPaymentStatusApi {
    private final PaymentService paymentService;

    @Override
    public ResponseEntity<PaymentStatusResponse> getPaymentStatus(
            UUID xRequestID,
            String authId,
            String paymentProduct,
            String bankId
    ) {
        log.debug("get payment status");


        return paymentService.getPaymentStatus(authId, paymentProduct, bankId);
    }
}

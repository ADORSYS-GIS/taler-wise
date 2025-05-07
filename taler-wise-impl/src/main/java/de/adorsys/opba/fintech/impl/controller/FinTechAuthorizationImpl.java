package de.adorsys.opba.fintech.impl.controller;

import de.adorsys.opba.fintech.api.resource.generated.FinTechAuthorizationApi;
import de.adorsys.opba.fintech.impl.controller.utils.OkOrNotOk;
import de.adorsys.opba.fintech.impl.database.entities.PaymentEntity;
import de.adorsys.opba.fintech.impl.database.repositories.PaymentRepository;
import de.adorsys.opba.fintech.impl.service.ConsentService;
import de.adorsys.opba.fintech.impl.service.RedirectHandlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;


@Slf4j
@RestController
@RequiredArgsConstructor
public class FinTechAuthorizationImpl implements FinTechAuthorizationApi {

    private final RedirectHandlerService redirectHandlerService;
    private final ConsentService consentService;
    private final PaymentRepository paymentRepository;

    @Override
    public ResponseEntity<Void> fromPaymentGET(String authId, String okOrNotokString, String finTechRedirectCode, UUID xRequestID) {
        OkOrNotOk okOrNotOk = OkOrNotOk.valueOf(okOrNotokString);

        if (okOrNotOk.equals(OkOrNotOk.OK) && consentService.confirmPayment(authId, xRequestID)) {

            Optional<PaymentEntity> payment = paymentRepository.findByTppAuthId(authId);

            if (!payment.isPresent()) {
                throw new RuntimeException("consent for authid " + authId + " can not be found");
            }

            log.debug("consent with authId {} is now valid", authId);
            payment.get().setPaymentConfirmed(true);
            paymentRepository.save(payment.get());
        }
        return redirectHandlerService.doRedirect(authId, finTechRedirectCode, okOrNotOk);
    }
}

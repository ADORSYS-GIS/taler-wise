package de.adorsys.opba.fintech.impl.service;

import de.adorsys.opba.fintech.impl.database.entities.ConsentEntity;
import de.adorsys.opba.fintech.impl.database.entities.PaymentEntity;
import de.adorsys.opba.fintech.impl.database.repositories.ConsentRepository;
import de.adorsys.opba.fintech.impl.database.repositories.PaymentRepository;
import de.adorsys.opba.fintech.impl.tppclients.ConsentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.UUID;

import static de.adorsys.opba.fintech.impl.tppclients.HeaderFields.FIN_TECH_REDIRECT_CODE;
import static de.adorsys.opba.fintech.impl.tppclients.HeaderFields.SERVICE_SESSION_ID;
import static de.adorsys.opba.fintech.impl.tppclients.HeaderFields.TPP_AUTH_ID;
import static org.springframework.http.HttpStatus.ACCEPTED;

@Service
@Slf4j
@RequiredArgsConstructor
public class HandleAcceptedService {

    ResponseEntity handleAccepted(ConsentRepository consentRepository, ConsentType consentType, String bankId,
                                  String fintechRedirectCode, HttpHeaders headers) {
        return handleAccepted(consentRepository, consentType, bankId, null, fintechRedirectCode, headers);
    }

    ResponseEntity handleAccepted(ConsentRepository consentRepository, ConsentType consentType, String bankId, String accountId,
                                  String fintechRedirectCode, HttpHeaders headers) {
        String authId = validateSession(headers);

        ConsentEntity consent = consentRepository.findByTppAuthId(authId)
                .orElseGet(() -> consentRepository.save(
                        new ConsentEntity(
                                consentType,
                                bankId,
                                accountId,
                                authId
                        ))
                );
        log.debug("created consent which is not confirmend yet for bank {}, user {}, type {}, auth {}",
                bankId, consentType, consent.getTppAuthId());
        consentRepository.save(consent);

        URI location = headers.getLocation();
        log.info("call was accepted, but redirect has to be done for authID:{} location:{}", consent.getTppAuthId(), location);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(FIN_TECH_REDIRECT_CODE, fintechRedirectCode);
        responseHeaders.setLocation(location);

        return new ResponseEntity<>(null, responseHeaders, ACCEPTED);
    }

    ResponseEntity handleAccepted(PaymentRepository paymentRepository, ConsentType consentType, String bankId, String accountId,
                                  String fintechRedirectCode, HttpHeaders headers, String paymentProduct) {

        String authId = validateSession(headers);
        PaymentEntity payment = paymentRepository.findByTppAuthId(authId)
                .orElseGet(() -> paymentRepository.save(
                        PaymentEntity.builder()

                                .bankId(bankId)
                                .accountId(accountId)
                                .paymentProduct(paymentProduct)
                                .tppServiceSessionId(UUID.fromString(headers.getFirst(SERVICE_SESSION_ID)))
                                .tppAuthId(authId)

                        .build()
                ));
        log.debug("created payment which is not confirmend yet for bank {}, user {}, type {}",
                bankId, "user identification", consentType);
        paymentRepository.save(payment);

        URI location = headers.getLocation();
        log.info("call was accepted, but redirect has to be done for location:{}", location);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(FIN_TECH_REDIRECT_CODE, fintechRedirectCode);
        responseHeaders.setLocation(location);
        responseHeaders.add(TPP_AUTH_ID, authId);

        return new ResponseEntity<>(null, responseHeaders, ACCEPTED);
    }

    private String validateSession(HttpHeaders headers) {
        if (StringUtils.isBlank(headers.getFirst(SERVICE_SESSION_ID))) {
            throw new RuntimeException("Did not expect headerfield " + SERVICE_SESSION_ID + " to be null");
        }
        if (StringUtils.isBlank(headers.getFirst(TPP_AUTH_ID))) {
            throw new RuntimeException("Did not expect headerfield " + TPP_AUTH_ID + " to be null");
        }
        return headers.getFirst(TPP_AUTH_ID);
    }
}

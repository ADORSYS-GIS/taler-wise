package de.adorsys.opba.fintech.impl.controller;

import de.adorsys.opba.fintech.api.model.generated.OrchestratedSinglePaymentInitiationRequest;
import de.adorsys.opba.fintech.api.resource.generated.FintechOrchestratedSinglePaymentInitiationApi;
import de.adorsys.opba.fintech.impl.service.OrchestratedPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

    @RestController
    @RequiredArgsConstructor
    @Slf4j
    public class FintechOrchestratedSinglePaymentInitiation implements FintechOrchestratedSinglePaymentInitiationApi {

        private final OrchestratedPaymentService paymentService;
        @Override
        @SneakyThrows
        public ResponseEntity<Void> initiateOrchestratedSinglePayment(UUID xRequestID,
                                                                      String fintechRedirectURLOK,
                                                                      String fintechRedirectURLNOK,
                                                                      String bankId,
                                                                      String accountId,
                                                                      OrchestratedSinglePaymentInitiationRequest body,
                                                                      Boolean fintechDecoupledPreferred,
                                                                      String fintechBrandLoggingInformation,
                                                                      String fintechNotificationURI,
                                                                      String fintechRedirectNotificationContentPreferred) {
            return paymentService.initiateOrchestratedSinglePayment(bankId,
                    accountId,
                    body,
                    fintechRedirectURLOK,
                    fintechRedirectURLNOK,
                    fintechDecoupledPreferred,
                    fintechBrandLoggingInformation,
                    fintechNotificationURI, fintechRedirectNotificationContentPreferred);
        }


    }


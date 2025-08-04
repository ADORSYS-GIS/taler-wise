package de.adorsys.opba.fintech.impl.controller;

import de.adorsys.opba.fintech.api.model.generated.PrivacyPolicy;
import de.adorsys.opba.fintech.api.model.generated.ServiceConfig;
import de.adorsys.opba.fintech.api.model.generated.TermsOfUse;
import de.adorsys.opba.fintech.api.resource.generated.FinTechConfigurationApi;
import de.adorsys.opba.fintech.impl.service.ServiceInfoService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ServiceInfoController implements FinTechConfigurationApi {

    private final ServiceInfoService serviceInfoService;

    @Override
    public ResponseEntity<ServiceConfig> getServiceConfig(UUID xRequestID) {
        log.debug("Getting service configuration, request ID: {}", xRequestID);

        try {
            ServiceConfig config = serviceInfoService.getServiceConfig();
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            log.error("Error retrieving service configuration", e);
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error retrieving service configuration"
            );
        }
    }

    @Override
    public ResponseEntity<TermsOfUse> getTermsOfUse(UUID xRequestID) {
        log.debug("Getting terms of use, request ID: {}", xRequestID);

        try {
            TermsOfUse terms = serviceInfoService.getTermsOfUse();
            return ResponseEntity.ok(terms);
        } catch (Exception e) {
            log.error("Error retrieving terms of use", e);
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error retrieving terms of use"
            );
        }
    }

    @Override
    public ResponseEntity<PrivacyPolicy> getPrivacyPolicy(UUID xRequestID) {
        log.debug("Getting privacy policy, request ID: {}", xRequestID);

        try {
            PrivacyPolicy privacyPolicy = serviceInfoService.getPrivacyPolicy();
            return ResponseEntity.ok(privacyPolicy);
        } catch (Exception e) {
            log.error("Error retrieving privacy policy", e);
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error retrieving privacy policy"
            );
        }
    }
}

package de.adorsys.opba.fintech.impl.controller;

import de.adorsys.opba.fintech.api.model.generated.InlineResponse2001;
import de.adorsys.opba.fintech.api.model.generated.InlineResponse2002;
import de.adorsys.opba.fintech.api.resource.generated.FinTechBankSearchApi;
import de.adorsys.opba.fintech.impl.service.BankSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class FinTechBankSearchImpl implements FinTechBankSearchApi {

    private final BankSearchService bankSearchService;

    @Override
    public ResponseEntity<InlineResponse2001> bankSearchGET(UUID xRequestID, String fintechToken, String keyword, Integer start, Integer max) {
        return new ResponseEntity<>(bankSearchService.searchBank(keyword, start, max), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<InlineResponse2002> bankProfileGET(UUID xRequestID, String fintechToken, String bankProfileId) {

        return new ResponseEntity<>(bankSearchService.searchBankProfile(bankProfileId), HttpStatus.OK);
    }
}

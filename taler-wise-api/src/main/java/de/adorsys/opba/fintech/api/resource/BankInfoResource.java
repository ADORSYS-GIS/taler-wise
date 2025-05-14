package de.adorsys.opba.fintech.api.resource;

import de.adorsys.opba.fintech.api.model.Bank;
import de.adorsys.opba.fintech.api.model.generated.BankInfoResponse;
import de.adorsys.opba.fintech.api.model.generated.V1BankInfoBody;
import de.adorsys.opba.fintech.api.repository.BankRepository;
import de.adorsys.opba.fintech.api.resource.generated.FinTechAuthorizationApi;
import org.iban4j.Iban;
import org.iban4j.IbanFormatException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class BankInfoResource implements FinTechAuthorizationApi {
    private final BankRepository bankRepository;

    @Autowired
    public BankInfoResource(BankRepository bankRepository) {
        this.bankRepository = bankRepository;
    }

    @PostMapping("/bankInfo")
    public ResponseEntity<BankInfoResponse> getBankInfoByIban(@RequestBody V1BankInfoBody body) {
        try {
            // Parse and validate the IBAN
            Iban iban = Iban.valueOf(body.getIban());
            System.out.println("Received IBAN: " + body.getIban());
            // Extract components
            String countryCode = iban.getCountryCode().getAlpha2();
            String bankCode = iban.getBankCode();
            String branchCode = iban.getBranchCode();

            // Retrieve bank metadata based on bank code
            Optional<Bank> bankOpt = bankRepository.findByBankCode(bankCode);

            if (bankOpt.isPresent()) {
                Bank bank = bankOpt.get();
                BankInfoResponse response = new BankInfoResponse();
                response.setCountryCode(countryCode);
                response.setBankCode(bankCode);
                response.setBranchCode(branchCode);
                response.setBankName(bank.getName());
                response.setBic(bank.getBic());

                return ResponseEntity.ok(response);
            } else {
                // Bank not found
                return ResponseEntity.notFound().build();
            }
        } catch (IbanFormatException e) {
            // Handle invalid IBAN format
            return ResponseEntity.badRequest().build();
        }
    }
}

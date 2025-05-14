package de.adorsys.opba.fintech.api.repository;

import de.adorsys.opba.fintech.api.model.Bank;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BankRepository extends JpaRepository<Bank, Long> {
    Optional<Bank> findByBankCode(String bankCode);
}

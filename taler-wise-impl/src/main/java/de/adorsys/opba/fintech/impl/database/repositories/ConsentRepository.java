package de.adorsys.opba.fintech.impl.database.repositories;

import de.adorsys.opba.fintech.impl.database.entities.ConsentEntity;
import de.adorsys.opba.fintech.impl.tppclients.ConsentType;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface ConsentRepository extends CrudRepository<ConsentEntity, Long> {
    Optional<ConsentEntity> findByTppAuthId(String authId);

    List<ConsentEntity> findByBankIdAndConsentTypeAndConsentConfirmedOrderByCreationTimeDesc(String bankid, ConsentType consentType, Boolean consentConfirmed);

    @Modifying
    long deleteByAccountIdAndBankId(String accountId, String bankId);
}

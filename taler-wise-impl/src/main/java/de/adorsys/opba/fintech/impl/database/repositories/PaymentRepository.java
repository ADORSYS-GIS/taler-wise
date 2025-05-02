package de.adorsys.opba.fintech.impl.database.repositories;

import de.adorsys.opba.fintech.impl.database.entities.PaymentEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends CrudRepository<PaymentEntity, Long> {
    Optional<PaymentEntity> findByTppAuthId(String authId);
    List<PaymentEntity> findByBankIdAndAccountIdAndPaymentConfirmed(String bankId, String accountId, Boolean paymentConfirmed);
}

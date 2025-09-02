package de.adorsys.opba.fintech.impl.mapper;

import de.adorsys.opba.fintech.api.model.generated.PaymentStatusResponse;
import org.mapstruct.Mapper;

@Mapper(implementationPackage = "de.adorsys.opba.fintech.impl.mapper.generated")
public interface PaymentStatusResponseMapper {

    PaymentStatusResponse mapFromTppToFintech(de.adorsys.opba.tpp.pis.api.model.generated.PaymentStatusResponse tppPaymentStatusResponse);

}

package de.adorsys.opba.fintech.impl.mapper;

import de.adorsys.opba.fintech.api.model.generated.InlineResponseBankInfo;
import org.mapstruct.factory.Mappers;

@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
public class ManualMapper {
    public static InlineResponseBankInfo fromTppToFintech(de.adorsys.opba.tpp.bankinfo.api.model.generated.BankInfoResponse bankInfoResponse) {
        return Mappers.getMapper(BankInfoMapper.class).mapFromTppToFintech(bankInfoResponse);
    }
}

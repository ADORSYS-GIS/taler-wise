package de.adorsys.opba.fintech.impl.tppclients;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.opba.tpp.pis.orchestrated.api.resource.generated.TppBankingApiOrchestratedSinglePaymentPisApi;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.cloud.openfeign.FeignClient;

import java.util.Optional;

@FeignClient(url = "${tpp.url}", name = "tppOrchestratedPaymentClient")
public interface TppOrchestratedPaymentClient extends TppBankingApiOrchestratedSinglePaymentPisApi {
    default Optional<ObjectMapper> getObjectMapper() {
        return Optional.empty();
    }

    default Optional<HttpServletRequest> getRequest() {
        return Optional.empty();
    }

    default Optional<String> getAcceptHeader() {
        return getRequest().map(r -> r.getHeader("Accept"));
    }
}

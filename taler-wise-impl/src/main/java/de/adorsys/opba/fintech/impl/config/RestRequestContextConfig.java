package de.adorsys.opba.fintech.impl.config;

import de.adorsys.opba.fintech.impl.controller.utils.RestRequestContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.RequestScope;

import jakarta.servlet.http.HttpServletRequest;

@Configuration
public class RestRequestContextConfig {

    @Bean
    @RequestScope
    public RestRequestContext provideRestRequestContext(HttpServletRequest httpServletRequest) {
        return RestRequestContext.builder()
                .uri(httpServletRequest.getRequestURI())
                .requestId(httpServletRequest.getHeader("X-Request-ID"))
                .build();
    }
}

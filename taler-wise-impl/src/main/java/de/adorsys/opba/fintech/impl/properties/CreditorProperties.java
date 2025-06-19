package de.adorsys.opba.fintech.impl.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "creditor")
public class CreditorProperties {
    private String name;
    private String iban;
}

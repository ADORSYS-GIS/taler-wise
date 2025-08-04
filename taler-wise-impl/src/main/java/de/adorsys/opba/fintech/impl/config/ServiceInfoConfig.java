package de.adorsys.opba.fintech.impl.config;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Configuration
@ConfigurationProperties("service-info")
public class ServiceInfoConfig {

    /**
     * Service provider configuration
     */
    private ServiceProvider serviceProvider = new ServiceProvider();

    /**
     * Terms of use configuration
     */
    private Terms terms = new Terms();

    /**
     * Privacy policy configuration
     */
    private Privacy privacy = new Privacy();

    @Data
    public static class ServiceProvider {

        @NotBlank
        private String name = "Taler-Wise Services";

        private String logoUrl = "https://adorsys.com/wp-content/uploads/2023/02/adorsys-logo-white-rgb.svg";
    }

    @Data
    public static class Terms {

        private String content =
            "Default terms of use content. Please configure this in application.yml";
        private String version = "1.0";
        private LocalDate lastUpdated = LocalDate.now();
        private String filePath; // Optional: path to external terms file
    }

    @Data
    public static class Privacy {

        private String content =
            "Default privacy policy content. Please configure this in application.yml";
        private String version = "1.0";
        private LocalDate lastUpdated = LocalDate.now();
        private String filePath; // Optional: path to external privacy file
    }
}

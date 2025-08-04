package de.adorsys.opba.fintech.impl.service;

import de.adorsys.opba.fintech.api.model.generated.PrivacyPolicy;
import de.adorsys.opba.fintech.api.model.generated.ServiceConfig;
import de.adorsys.opba.fintech.api.model.generated.TermsOfUse;
import de.adorsys.opba.fintech.impl.config.ServiceInfoConfig;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceInfoService {

    private final ServiceInfoConfig serviceInfoConfig;

    public ServiceConfig getServiceConfig() {
        log.debug("Building service configuration");

        ServiceConfig config = new ServiceConfig();
        ServiceInfoConfig.ServiceProvider provider =
            serviceInfoConfig.getServiceProvider();

        config.setServiceProviderName(provider.getName());
        config.setLogoUrl(provider.getLogoUrl());

        return config;
    }

    public TermsOfUse getTermsOfUse() throws IOException {
        log.debug("Building terms of use");

        ServiceInfoConfig.Terms termsConfig = serviceInfoConfig.getTerms();
        TermsOfUse terms = new TermsOfUse();

        // Try to load from external file if configured
        String content = loadContentFromFileOrDefault(
            termsConfig.getFilePath(),
            termsConfig.getContent()
        );

        terms.setContent(content);
        terms.setVersion(termsConfig.getVersion());
        terms.setLastUpdated(termsConfig.getLastUpdated());

        return terms;
    }

    public PrivacyPolicy getPrivacyPolicy() throws IOException {
        log.debug("Building privacy policy");

        ServiceInfoConfig.Privacy privacyConfig =
            serviceInfoConfig.getPrivacy();
        PrivacyPolicy privacy = new PrivacyPolicy();

        // Try to load from external file if configured
        String content = loadContentFromFileOrDefault(
            privacyConfig.getFilePath(),
            privacyConfig.getContent()
        );

        privacy.setContent(content);
        privacy.setVersion(privacyConfig.getVersion());
        privacy.setLastUpdated(privacyConfig.getLastUpdated());

        return privacy;
    }

    private String loadContentFromFileOrDefault(
        String filePath,
        String defaultContent
    ) throws IOException {
        if (filePath == null || filePath.trim().isEmpty()) {
            return defaultContent;
        }

        try {
            // Try as classpath resource first
            if (filePath.startsWith("classpath:")) {
                String resourcePath = filePath.substring("classpath:".length());
                ClassPathResource resource = new ClassPathResource(
                    resourcePath
                );
                if (resource.exists()) {
                    return StreamUtils.copyToString(
                        resource.getInputStream(),
                        StandardCharsets.UTF_8
                    );
                }
            }

            // Try as file system path
            Path path = Path.of(filePath);
            if (Files.exists(path)) {
                return Files.readString(path, StandardCharsets.UTF_8);
            }

            log.warn(
                "Could not find file at path: {}, using default content",
                filePath
            );
            return defaultContent;
        } catch (Exception e) {
            log.error(
                "Error loading content from file: {}, using default content",
                filePath,
                e
            );
            return defaultContent;
        }
    }
}

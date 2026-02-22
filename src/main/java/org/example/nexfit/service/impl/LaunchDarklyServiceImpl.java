package org.example.nexfit.service.impl;

import com.launchdarkly.sdk.LDContext;
import com.launchdarkly.sdk.server.LDClient;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.example.nexfit.service.LaunchDarklyService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class LaunchDarklyServiceImpl implements LaunchDarklyService {

    @Value("${launchdarkly.enabled:false}")
    private boolean enabled;

    @Value("${launchdarkly.sdk-key:}")
    private String sdkKey;

    @Value("${launchdarkly.flag-key:verified-trainers}")
    private String flagKey;

    private LDClient ldClient;

    @PostConstruct
    public void init() {
        if (!enabled) {
            log.info("LaunchDarkly disabled");
            return;
        }
        if (sdkKey == null || sdkKey.isBlank()) {
            log.warn("LaunchDarkly enabled but SDK key not provided. Disabling LaunchDarkly.");
            enabled = false;
            return;
        }

        try {
            ldClient = new LDClient(sdkKey);
            log.info("LaunchDarkly client initialized for flag {}", flagKey);
        } catch (Exception e) {
            log.error("Failed to initialize LaunchDarkly client", e);
            enabled = false;
        }
    }

    @PreDestroy
    public void shutdown() {
        if (ldClient != null) {
            try {
                ldClient.close();
            } catch (IOException e) {
                log.warn("Failed to close LaunchDarkly client", e);
            }
        }
    }

    @Override
    public boolean isTrainerVerified(String trainerEmail) {
        if (!enabled || ldClient == null) {
            log.debug("LaunchDarkly disabled — treating all trainers as verified");
            return true;
        }
        if (trainerEmail == null || trainerEmail.isBlank()) {
            log.warn("LaunchDarkly evaluation skipped: trainer email is missing");
            return false;
        }

        try {
            LDContext context = LDContext.builder(trainerEmail)
                    .kind("user")
                    .set("email", trainerEmail)
                    .build();
            return ldClient.boolVariation(flagKey, context, false);
        } catch (Exception e) {
            log.error("LaunchDarkly evaluation failed for trainer {}", trainerEmail, e);
            return false;
        }
    }
}

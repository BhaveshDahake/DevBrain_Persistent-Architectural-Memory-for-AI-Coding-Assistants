package com.example.DevBrain.health;

import com.example.DevBrain.service.CogneeAvailabilityService;
import com.example.DevBrain.service.LocalRepositorySearchService;
import org.springframework.stereotype.Component;

@Component("productionHealthIndicator")
public class ProductionReadinessHealthIndicator {

    private final CogneeAvailabilityService availabilityService;
    private final LocalRepositorySearchService localRepositorySearchService;

    public ProductionReadinessHealthIndicator(CogneeAvailabilityService availabilityService,
                                              LocalRepositorySearchService localRepositorySearchService) {
        this.availabilityService = availabilityService;
        this.localRepositorySearchService = localRepositorySearchService;
    }

    public String getStatus() {
        boolean repositoryReady = localRepositorySearchService != null;
        boolean cogneeHealthy = availabilityService.canAttempt();
        boolean fallbackActive = availabilityService.isFallbackActive();
        return repositoryReady && (cogneeHealthy || fallbackActive) ? "UP" : "DOWN";
    }
}

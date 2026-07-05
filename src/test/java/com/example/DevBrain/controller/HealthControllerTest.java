package com.example.DevBrain.controller;

import com.example.DevBrain.service.CogneeAvailabilityService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.info.BuildProperties;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HealthControllerTest {

    @Test
    void healthEndpointReturnsServiceStatus() throws Exception {
        CogneeAvailabilityService availabilityService = mock(CogneeAvailabilityService.class);
        when(availabilityService.getCurrentStatus()).thenReturn("HEALTHY");
        when(availabilityService.isFallbackActive()).thenReturn(false);
        when(availabilityService.getFailureThreshold()).thenReturn(3);
        when(availabilityService.getCooldown()).thenReturn(java.time.Duration.ofMinutes(1));

        Properties properties = new Properties();
        properties.setProperty("version", "1.2.3");
        properties.setProperty("time", "2026-07-05T00:00:00Z");
        BuildProperties buildProperties = new BuildProperties(properties);

        HealthController hc = new HealthController(availabilityService, buildProperties);
        var resp = hc.health();
        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        var body = resp.getBody();
        assertThat(body).containsEntry("status", "UP");
        assertThat(body).containsEntry("service", "devbrain-backend");
        assertThat(body).containsEntry("version", "1.2.3");
        assertThat(body).containsEntry("buildTimestamp", "2026-07-05T00:00:00Z");
    }

    @Test
    void readinessAndLivenessEndpointsExposeOperationalStatus() {
        CogneeAvailabilityService availabilityService = mock(CogneeAvailabilityService.class);
        when(availabilityService.getCurrentStatus()).thenReturn("HEALTHY");
        when(availabilityService.isFallbackActive()).thenReturn(false);
        when(availabilityService.getFailureThreshold()).thenReturn(3);
        when(availabilityService.getCooldown()).thenReturn(java.time.Duration.ofMinutes(1));

        Properties properties = new Properties();
        properties.setProperty("version", "1.2.3");
        properties.setProperty("time", "2026-07-05T00:00:00Z");
        BuildProperties buildProperties = new BuildProperties(properties);

        HealthController hc = new HealthController(availabilityService, buildProperties);

        var liveness = hc.liveness();
        assertThat(liveness.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(liveness.getBody()).containsEntry("status", "UP");

        var readiness = hc.readiness();
        assertThat(readiness.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(readiness.getBody()).containsEntry("status", "UP");
        assertThat(readiness.getBody()).containsEntry("mode", "CLOUD");
    }
}

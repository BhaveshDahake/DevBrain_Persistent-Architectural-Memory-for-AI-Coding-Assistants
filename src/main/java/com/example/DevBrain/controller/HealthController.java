package com.example.DevBrain.controller;

import com.example.DevBrain.service.CogneeAvailabilityService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.LinkedHashMap;

@RestController
@RequestMapping("/api")
public class HealthController {

    private final CogneeAvailabilityService availabilityService;
    private final BuildProperties buildProperties;

    public HealthController(CogneeAvailabilityService availabilityService, BuildProperties buildProperties) {
        this.availabilityService = availabilityService;
        this.buildProperties = buildProperties;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "UP");
        response.put("service", "devbrain-backend");
        response.put("timestamp", System.currentTimeMillis());
        response.put("version", buildProperties.getVersion());
        response.put("buildTimestamp", formatBuildTimestamp());
        response.put("cogneeStatus", availabilityService.getCurrentStatus());
        response.put("fallbackActive", availabilityService.isFallbackActive());
        response.put("currentMode", availabilityService.isFallbackActive() ? "LOCAL" : "CLOUD");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/system/status")
    public ResponseEntity<Map<String, Object>> systemStatus() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("applicationStatus", "UP");
        response.put("cogneeStatus", availabilityService.getCurrentStatus());
        response.put("fallbackStatus", availabilityService.isFallbackActive() ? "ACTIVE" : "INACTIVE");
        response.put("failureThreshold", availabilityService.getFailureThreshold());
        response.put("cooldownSeconds", availabilityService.getCooldown().getSeconds());
        response.put("currentMode", availabilityService.isFallbackActive() ? "LOCAL" : "CLOUD");
        response.put("version", buildProperties.getVersion());
        response.put("buildTimestamp", formatBuildTimestamp());
        response.put("searchMode", availabilityService.isFallbackActive() ? "LOCAL" : "CLOUD");
        response.put("uploadMode", availabilityService.isFallbackActive() ? "LOCAL" : "CLOUD");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health/liveness")
    public ResponseEntity<Map<String, Object>> liveness() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "UP");
        response.put("service", "devbrain-backend");
        response.put("mode", "RUNNING");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health/readiness")
    public ResponseEntity<Map<String, Object>> readiness() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", availabilityService.isFallbackActive() ? "UP" : "UP");
        response.put("service", "devbrain-backend");
        response.put("cogneeStatus", availabilityService.getCurrentStatus());
        response.put("fallbackActive", availabilityService.isFallbackActive());
        response.put("mode", availabilityService.isFallbackActive() ? "LOCAL" : "CLOUD");
        return ResponseEntity.ok(response);
    }

    private String formatBuildTimestamp() {
        return buildProperties.getTime() != null ? buildProperties.getTime().toString() : null;
    }
}

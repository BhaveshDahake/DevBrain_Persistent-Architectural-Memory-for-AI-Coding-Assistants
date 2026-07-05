package com.example.DevBrain.controller;

import org.springframework.boot.info.BuildProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DeploymentInfoController {

    private final BuildProperties buildProperties;

    public DeploymentInfoController(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    @GetMapping("/deployment/info")
    public ResponseEntity<Map<String, Object>> deploymentInfo() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("service", "devbrain-backend");
        response.put("version", buildProperties.getVersion());
        response.put("buildTimestamp", buildProperties.getTime() != null ? buildProperties.getTime().toString() : null);
        response.put("artifact", buildProperties.getArtifact());
        response.put("group", buildProperties.getGroup());
        return ResponseEntity.ok(response);
    }
}

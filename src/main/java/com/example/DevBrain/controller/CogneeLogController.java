package com.example.DevBrain.controller;

import com.example.DevBrain.service.CogneeClientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cognee")
@CrossOrigin(origins = "*")
public class CogneeLogController {

    @GetMapping("/logs")
    public ResponseEntity<List<Map<String, Object>>> getLogs() {
        return ResponseEntity.ok(CogneeClientService.getApiLogs());
    }

    @DeleteMapping("/logs")
    public ResponseEntity<Void> clearLogs() {
        CogneeClientService.clearApiLogs();
        return ResponseEntity.noContent().build();
    }
}

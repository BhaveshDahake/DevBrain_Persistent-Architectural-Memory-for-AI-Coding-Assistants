package com.example.DevBrain.controller;

import com.example.DevBrain.dto.ResetContextResponse;
import com.example.DevBrain.service.ContextResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/memory")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ContextController {

    private final ContextResetService contextResetService;

    @DeleteMapping("/context/{datasetName}")
    public ResponseEntity<ResetContextResponse> resetContext(
            @PathVariable String datasetName,
            jakarta.servlet.http.HttpServletRequest request) {
        
        String userId = (String) request.getAttribute("userId");
        String scopedDatasetName = (userId != null) ? userId + "_" + datasetName : datasetName;
        
        ResetContextResponse response = contextResetService.reset(scopedDatasetName);
        return ResponseEntity.ok(response);
    }
}

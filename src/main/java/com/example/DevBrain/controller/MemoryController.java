package com.example.DevBrain.controller;

import com.example.DevBrain.dto.ImproveMemoryRequest;
import com.example.DevBrain.dto.ImproveMemoryResponse;
import com.example.DevBrain.service.MemoryImprovementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/memory")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MemoryController {

    private final MemoryImprovementService memoryImprovementService;

    @PostMapping("/improve")
    public ResponseEntity<ImproveMemoryResponse> improveMemory(
            @Valid @RequestBody ImproveMemoryRequest request,
            jakarta.servlet.http.HttpServletRequest servletRequest) {
        
        String userId = (String) servletRequest.getAttribute("userId");
        if (userId != null) {
            request.setDatasetName(userId + "_" + request.getDatasetName());
        }
        
        ImproveMemoryResponse response = memoryImprovementService.improve(request);
        return ResponseEntity.ok(response);
    }
}

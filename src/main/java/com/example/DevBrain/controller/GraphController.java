package com.example.DevBrain.controller;

import com.example.DevBrain.dto.GraphResponse;
import com.example.DevBrain.service.GraphInspectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/graph")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GraphController {

    private final GraphInspectionService graphInspectionService;

    @GetMapping("/{datasetName}")
    public ResponseEntity<GraphResponse> inspectGraph(
            @PathVariable String datasetName,
            jakarta.servlet.http.HttpServletRequest request) {
        
        String userId = (String) request.getAttribute("userId");
        String scopedDatasetName = (userId != null) ? userId + "_" + datasetName : datasetName;
        
        GraphResponse response = graphInspectionService.inspectGraph(scopedDatasetName);
        if (Boolean.TRUE.equals(response.getSuccess())) {
            return ResponseEntity.ok(response);
        } else {
            // Can map to 503 per prompt or return generic bad request. We will use 500/503 for unavailable.
            // Since error message might just be generic, we'll return 500 Internal Server Error but with 200 payload 
            // format or 503 Service Unavailable depending on strictly following HTTP semantics.
            return ResponseEntity.status(503).body(response);
        }
    }
}

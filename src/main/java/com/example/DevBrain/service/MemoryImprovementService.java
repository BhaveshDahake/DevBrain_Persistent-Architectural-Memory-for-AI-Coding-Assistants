package com.example.DevBrain.service;

import com.example.DevBrain.dto.ImproveMemoryRequest;
import com.example.DevBrain.dto.ImproveMemoryResponse;

public interface MemoryImprovementService {
    ImproveMemoryResponse improve(ImproveMemoryRequest request);
}

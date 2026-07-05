package com.example.DevBrain.service;

import com.example.DevBrain.dto.ResetContextResponse;

public interface ContextResetService {
    ResetContextResponse reset(String datasetName);
}

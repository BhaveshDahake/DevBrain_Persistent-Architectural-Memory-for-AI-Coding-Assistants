package com.example.DevBrain.service;

import com.example.DevBrain.dto.GraphResponse;

public interface GraphInspectionService {
    GraphResponse inspectGraph(String datasetName);
}

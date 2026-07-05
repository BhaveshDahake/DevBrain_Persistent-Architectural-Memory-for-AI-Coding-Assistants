package com.example.DevBrain.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GraphFetchRequest {
    private List<String> datasets;
}

package com.example.DevBrain.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RepositoryContext {
    private String file;
    private String function;
    private String summary;
    private Double score;
}

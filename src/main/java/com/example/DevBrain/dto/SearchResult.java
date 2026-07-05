package com.example.DevBrain.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SearchResult {
    private String answer;
    private List<RepositoryContext> contexts;
    private boolean processing;
    private String status;
    private String source;
}

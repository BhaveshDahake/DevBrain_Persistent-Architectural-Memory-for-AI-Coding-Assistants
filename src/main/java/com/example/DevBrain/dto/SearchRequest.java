package com.example.DevBrain.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SearchRequest {
    private String query;
    private List<String> datasets;
    
    @Builder.Default
    private SearchType searchType = SearchType.GRAPH_COMPLETION;
    
    @Builder.Default
    private int maxResults = 10;
}

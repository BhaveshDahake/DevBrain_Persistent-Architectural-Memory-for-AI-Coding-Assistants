package com.example.DevBrain.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "graph")
public class GraphProperties {
    
    private int maxNodes = 3000;
    private int maxLinks = 5000;

    public int getMaxNodes() { return maxNodes; }
    public void setMaxNodes(int maxNodes) { this.maxNodes = maxNodes; }

    public int getMaxLinks() { return maxLinks; }
    public void setMaxLinks(int maxLinks) { this.maxLinks = maxLinks; }
}

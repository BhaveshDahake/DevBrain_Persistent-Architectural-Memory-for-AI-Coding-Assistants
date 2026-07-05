package com.example.DevBrain.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "dataset")
public class DatasetProperties {
    
    private DataSize maxZipSize = DataSize.ofMegabytes(500);
    private DataSize maxEntrySize = DataSize.ofMegabytes(20);
    private String workspace = "workspace";
    private List<String> ignoredFolders;
    private List<String> ignoredExtensions;
    private List<String> allowedExtensions;

    public DataSize getMaxZipSize() { return maxZipSize; }
    public void setMaxZipSize(DataSize maxZipSize) { this.maxZipSize = maxZipSize; }

    public DataSize getMaxEntrySize() { return maxEntrySize; }
    public void setMaxEntrySize(DataSize maxEntrySize) { this.maxEntrySize = maxEntrySize; }

    public String getWorkspace() { return workspace; }
    public void setWorkspace(String workspace) { this.workspace = workspace; }

    public List<String> getIgnoredFolders() { return ignoredFolders; }
    public void setIgnoredFolders(List<String> ignoredFolders) { this.ignoredFolders = ignoredFolders; }

    public List<String> getIgnoredExtensions() { return ignoredExtensions; }
    public void setIgnoredExtensions(List<String> ignoredExtensions) { this.ignoredExtensions = ignoredExtensions; }

    public List<String> getAllowedExtensions() { return allowedExtensions; }
    public void setAllowedExtensions(List<String> allowedExtensions) { this.allowedExtensions = allowedExtensions; }
}

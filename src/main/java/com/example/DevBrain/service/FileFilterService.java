package com.example.DevBrain.service;

import com.example.DevBrain.config.DatasetProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class FileFilterService {

    private final DatasetProperties properties;

    public FileFilterService(DatasetProperties properties) {
        this.properties = properties;
    }

    public boolean shouldKeepFolder(String folderName) {
        if (!StringUtils.hasText(folderName)) return true;
        if (properties.getIgnoredFolders() == null) return true;
        
        // Check if any part of the path matches an ignored folder (e.g. node_modules/express)
        Path path = Paths.get(folderName);
        for (Path part : path) {
            String partStr = part.toString();
            if (properties.getIgnoredFolders().contains(partStr)) {
                return false;
            }
        }
        return true;
    }

    public boolean shouldKeepFile(String fileName) {
        if (!StringUtils.hasText(fileName)) return false;

        // Check if file extension is explicitly ignored
        if (properties.getIgnoredExtensions() != null) {
            for (String ext : properties.getIgnoredExtensions()) {
                if (fileName.toLowerCase().endsWith(ext.toLowerCase())) {
                    return false;
                }
            }
        }
        
        // Keep ONLY raw source/text files matching the allowed extensions blocklist
        if (properties.getAllowedExtensions() != null) {
            for (String ext : properties.getAllowedExtensions()) {
                // E.g., .java or explicit names like Dockerfile
                if (fileName.toLowerCase().endsWith(ext.toLowerCase()) || fileName.equalsIgnoreCase(ext)) {
                    return true;
                }
            }
            return false; // Not in allowed list
        }

        // If no allowed list is defined, fallback to keeping it
        return true;
    }
}

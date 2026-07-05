package com.example.DevBrain.service;

import com.example.DevBrain.config.DatasetProperties;
import com.example.DevBrain.dto.ExtractionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.stream.Stream;

@Service
public class ZipExtractor {

    private static final Logger log = LoggerFactory.getLogger(ZipExtractor.class);
    
    private final DatasetProperties properties;
    private final FileFilterService fileFilterService;

    public ZipExtractor(DatasetProperties properties, FileFilterService fileFilterService) {
        this.properties = properties;
        this.fileFilterService = fileFilterService;
    }

    public ExtractionResult extract(Path zipFilePath, String datasetName) {
        long startTime = System.currentTimeMillis();
        long totalFiles = 0;
        long keptFiles = 0;
        long skippedFiles = 0;

        Path workspaceCleanPath = Paths.get(properties.getWorkspace(), datasetName, "clean").toAbsolutePath().normalize();
        
        // Scan current files before cleaning up the directory
        java.util.Map<String, Long> oldFiles = scanDirectory(workspaceCleanPath);
        deleteDirectoryContents(workspaceCleanPath);

        try {
            // Prevent Zip Bomb at the overall file level
            if (Files.size(zipFilePath) > properties.getMaxZipSize().toBytes()) {
                throw new IllegalArgumentException("ZIP file exceeds maximum allowed size.");
            }

            Files.createDirectories(workspaceCleanPath);
            log.info("Started extracting dataset '{}' into {}", datasetName, workspaceCleanPath);

            try (InputStream is = Files.newInputStream(zipFilePath);
                 ZipInputStream zis = new ZipInputStream(is)) {
                
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    if (entry.isDirectory()) {
                        zis.closeEntry();
                        continue; // We only process files, directories are recreated dynamically for allowed files
                    }
                    totalFiles++;

                    String entryName = entry.getName();
                    
                    // 1. Zip Slip Prevention
                    Path targetPath = workspaceCleanPath.resolve(entryName).normalize();
                    if (!targetPath.startsWith(workspaceCleanPath)) {
                        log.warn("Zip Slip attempt detected! Skipping path traversal entry: {}", entryName);
                        skippedFiles++;
                        zis.closeEntry();
                        continue;
                    }

                    // 2. Apply Folder & File Cleaning Rules
                    Path parentDir = targetPath.getParent();
                    String relativeParent = workspaceCleanPath.relativize(parentDir).toString();
                    
                    if (!fileFilterService.shouldKeepFolder(relativeParent)) {
                        log.debug("Skipping file in ignored folder: {}", entryName);
                        skippedFiles++;
                        zis.closeEntry();
                        continue;
                    }
                    
                    String fileName = targetPath.getFileName().toString();
                    if (!fileFilterService.shouldKeepFile(fileName)) {
                        log.debug("Skipping ignored file type: {}", entryName);
                        skippedFiles++;
                        zis.closeEntry();
                        continue;
                    }

                    // 3. Zip Bomb Prevention (Per-Entry Size limit)
                    // Note: size can be -1 if unknown in stream, we also check while reading byte chunks
                    if (entry.getSize() > properties.getMaxEntrySize().toBytes()) {
                        log.warn("Entry exceeds maximum size limit ({} bytes): {}", properties.getMaxEntrySize().toBytes(), entryName);
                        skippedFiles++;
                        zis.closeEntry();
                        continue;
                    }

                    // 4. Safe Extraction
                    Files.createDirectories(parentDir);
                    boolean extractedSuccessfully = extractEntry(zis, targetPath, properties.getMaxEntrySize().toBytes());
                    
                    if (extractedSuccessfully) {
                        keptFiles++;
                    } else {
                        skippedFiles++; // Skipped because it blew past max size during chunked read
                    }
                    zis.closeEntry();
                }
            }

        } catch (IOException e) {
            log.error("Error during ZIP extraction for dataset {}", datasetName, e);
            throw new RuntimeException("Extraction failed for dataset: " + e.getMessage(), e);
        }

        long processingTimeMs = System.currentTimeMillis() - startTime;
        log.info("Extraction completed for dataset '{}'. Total: {}, Kept: {}, Skipped: {}, Time: {}ms", 
                 datasetName, totalFiles, keptFiles, skippedFiles, processingTimeMs);

        // Scan after extraction
        java.util.Map<String, Long> newFiles = scanDirectory(workspaceCleanPath);

        java.util.List<String> addedFiles = new java.util.ArrayList<>();
        java.util.List<String> modifiedFiles = new java.util.ArrayList<>();
        java.util.List<String> removedFiles = new java.util.ArrayList<>();

        // Only calculate details if this is a modification (i.e. oldFiles was not empty)
        if (oldFiles != null && !oldFiles.isEmpty()) {
            for (java.util.Map.Entry<String, Long> entry : newFiles.entrySet()) {
                String path = entry.getKey();
                Long size = entry.getValue();
                if (!oldFiles.containsKey(path)) {
                    addedFiles.add(path);
                } else if (!oldFiles.get(path).equals(size)) {
                    modifiedFiles.add(path);
                }
            }
            for (String path : oldFiles.keySet()) {
                if (!newFiles.containsKey(path)) {
                    removedFiles.add(path);
                }
            }
        }

        ExtractionResult result = new ExtractionResult();
        result.setDatasetName(datasetName);
        result.setTotalFiles(totalFiles);
        result.setKeptFiles(keptFiles);
        result.setSkippedFiles(skippedFiles);
        result.setCleanPath(workspaceCleanPath.toString());
        result.setProcessingTimeMs(processingTimeMs);
        result.setAddedFiles(addedFiles);
        result.setModifiedFiles(modifiedFiles);
        result.setRemovedFiles(removedFiles);

        return result;
    }

    /**
     * Extracts a single entry stream securely chunk-by-chunk to prevent memory overflows.
     * Returns true if successful, false if limit exceeded mid-stream.
     */
    private boolean extractEntry(ZipInputStream zis, Path targetPath, long maxSize) throws IOException {
        try (OutputStream os = Files.newOutputStream(targetPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            byte[] buffer = new byte[8192];
            long totalRead = 0;
            int read;
            while ((read = zis.read(buffer)) != -1) {
                totalRead += read;
                if (totalRead > maxSize) {
                    log.warn("Entry decompressed stream blew past max size limit. Deleting partial file: {}", targetPath);
                    os.close();
                    Files.deleteIfExists(targetPath);
                    return false;
                }
                os.write(buffer, 0, read);
            }
            return true;
        }
    }

    private java.util.Map<String, Long> scanDirectory(Path dir) {
        java.util.Map<String, Long> fileMap = new java.util.HashMap<>();
        if (!Files.exists(dir) || !Files.isDirectory(dir)) {
            return fileMap;
        }
        try (Stream<Path> paths = Files.walk(dir)) {
            paths.filter(Files::isRegularFile).forEach(path -> {
                String relative = dir.relativize(path).toString().replace("\\", "/");
                try {
                    fileMap.put(relative, Files.size(path));
                } catch (IOException e) {
                    fileMap.put(relative, 0L);
                }
            });
        } catch (IOException e) {
            log.warn("Failed to scan directory for diff: {}", dir, e);
        }
        return fileMap;
    }

    private void deleteDirectoryContents(Path dir) {
        if (!Files.exists(dir)) return;
        try (Stream<Path> paths = Files.walk(dir)) {
            paths.sorted(java.util.Comparator.reverseOrder())
                 .forEach(path -> {
                     if (!path.equals(dir)) {
                         try {
                             Files.delete(path);
                         } catch (IOException e) {
                             log.warn("Failed to delete path during cleanup: {}", path, e);
                         }
                     }
                 });
        } catch (IOException e) {
            log.warn("Failed to clear directory: {}", dir, e);
        }
    }
}

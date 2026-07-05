package com.example.DevBrain.service;

import com.example.DevBrain.config.DatasetProperties;
import com.example.DevBrain.config.GraphProperties;
import com.example.DevBrain.config.DemoInitializer;
import com.example.DevBrain.dto.GraphLink;
import com.example.DevBrain.dto.GraphNode;
import com.example.DevBrain.dto.GraphQueryResult;
import com.example.DevBrain.dto.GraphResponse;
import com.example.DevBrain.exception.DatasetNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GraphInspectionServiceImpl implements GraphInspectionService {

    private final CogneeClientService cogneeClientService;
    private final DatasetProperties datasetProperties;
    private final GraphProperties graphProperties;

    @Override
    public GraphResponse inspectGraph(String datasetName) {
        log.info("Inspecting knowledge graph for dataset: {}", datasetName);
        long startTime = System.currentTimeMillis();

        // 0. Auto-initialize demo repo if missing on disk (e.g. after Reset Context)
        if ("devbrain-demo-repo".equals(datasetName)) {
            DemoInitializer.initializeDemoRepo(datasetProperties.getWorkspace(), datasetName);
        }

        // 1. Validate Dataset
        Path workspacePath = Paths.get(datasetProperties.getWorkspace(), datasetName).toAbsolutePath().normalize();
        if (!Files.exists(workspacePath)) {
            log.error("Dataset not found: {}", datasetName);
            throw new DatasetNotFoundException("Dataset '" + datasetName + "' was not found.");
        }

        // If mock mode is active, directly bypass Cognee query and build filesystem fallback graph
        // so the user actually sees their uploaded folders/services instead of static mock classes!
        if (cogneeClientService.isMock()) {
            log.info("Mock mode is enabled. Rendering filesystem fallback graph directly for: {}", datasetName);
            return buildFilesystemFallbackGraph(datasetName, workspacePath.resolve("clean"));
        }

        // 2. Query Cognee Graph
        GraphQueryResult rawResult = cogneeClientService.fetchGraph(datasetName);

        // 3. Normalize Nodes and Links (Handles Pattern A & B)
        List<Map<String, Object>> rawNodes = rawResult.getNodes() != null ? rawResult.getNodes() :
                                             (rawResult.getEntities() != null ? rawResult.getEntities() : Collections.emptyList());

        List<Map<String, Object>> rawEdges = rawResult.getEdges() != null ? rawResult.getEdges() :
                                             (rawResult.getRelationships() != null ? rawResult.getRelationships() : Collections.emptyList());

        List<GraphNode> nodes = parseNodes(rawNodes);
        List<GraphLink> links = parseLinks(rawEdges);

        if (nodes.isEmpty()) {
            log.info("Cognee graph is empty for dataset '{}'. Returning an empty graph response.", datasetName);
            return GraphResponse.builder()
                    .success(true)
                    .message("Graph inspected successfully")
                    .nodes(Collections.emptyList())
                    .links(Collections.emptyList())
                    .build();
        }

        // 4. Enforce Performance Limits
        if (nodes.size() > graphProperties.getMaxNodes()) {
            log.warn("Truncating nodes from {} to max {}", nodes.size(), graphProperties.getMaxNodes());
            nodes = nodes.subList(0, graphProperties.getMaxNodes());
        }
        if (links.size() > graphProperties.getMaxLinks()) {
            log.warn("Truncating links from {} to max {}", links.size(), graphProperties.getMaxLinks());
            links = links.subList(0, graphProperties.getMaxLinks());
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("Graph inspection completed in {}ms. Found {} nodes, {} links.", duration, nodes.size(), links.size());

        return GraphResponse.builder()
                .success(true)
                .message("Graph inspected successfully")
                .nodes(nodes)
                .links(links)
                .build();
    }

    private GraphResponse buildFilesystemFallbackGraph(String datasetName, Path workspaceCleanPath) {
        log.info("Building filesystem fallback graph for dataset: {}", datasetName);
        List<GraphNode> nodes = new ArrayList<>();
        List<GraphLink> links = new ArrayList<>();
        
        if (!Files.exists(workspaceCleanPath) || !Files.isDirectory(workspaceCleanPath)) {
            log.error("Clean directory does not exist for fallback graph: {}", workspaceCleanPath);
            return GraphResponse.builder()
                    .success(false)
                    .message("No repository graph available")
                    .nodes(Collections.emptyList())
                    .links(Collections.emptyList())
                    .build();
        }

        try (var paths = Files.walk(workspaceCleanPath)) {
            List<Path> allPaths = paths.collect(Collectors.toList());
            
            // First pass: add nodes
            for (Path p : allPaths) {
                if (p.equals(workspaceCleanPath)) continue;
                String relative = workspaceCleanPath.relativize(p).toString().replace('\\', '/');
                String label = p.getFileName().toString();
                boolean isDir = Files.isDirectory(p);
                
                String type = isDir ? "DIRECTORY" : "FILE";
                if (!isDir) {
                    if (label.endsWith("Service.java") || label.endsWith("ServiceImpl.java") || label.toLowerCase().contains("service")) {
                        type = "SERVICE";
                    } else if (label.endsWith("Interface.java") || label.toLowerCase().contains("interface")) {
                        type = "INTERFACE";
                    } else if (label.endsWith(".java") || label.endsWith(".class")) {
                        type = "CLASS";
                    } else if (label.endsWith("Controller.java")) {
                        type = "CLASS";
                    } else if (label.endsWith(".jsx") || label.endsWith(".tsx") || label.endsWith(".js") || label.endsWith(".ts")) {
                        type = "FILE";
                    }
                }
                
                nodes.add(GraphNode.builder()
                        .id(relative)
                        .label(label)
                        .type(type)
                        .path(relative)
                        .build());
            }
            
            // Second pass: add links (hierarchical structure)
            for (Path p : allPaths) {
                if (p.equals(workspaceCleanPath)) continue;
                Path parent = p.getParent();
                if (parent != null && !parent.equals(workspaceCleanPath)) {
                    String relativeSrc = workspaceCleanPath.relativize(parent).toString().replace('\\', '/');
                    String relativeTgt = workspaceCleanPath.relativize(p).toString().replace('\\', '/');
                    links.add(GraphLink.builder()
                            .source(relativeSrc)
                            .target(relativeTgt)
                            .relationship("CONTAINS")
                            .weight(1.0)
                            .build());
                }
            }
            
            // Third pass: dependency linking by simple keyword matching
            for (Path p : allPaths) {
                if (Files.isRegularFile(p)) {
                    String relativeSrc = workspaceCleanPath.relativize(p).toString().replace('\\', '/');
                    String content = Files.readString(p);
                    for (GraphNode otherNode : nodes) {
                        if ("DIRECTORY".equals(otherNode.getType())) continue;
                        if (otherNode.getId().equals(relativeSrc)) continue;
                        
                        String targetBaseName = otherNode.getLabel().replaceAll("\\.[^.]+$", "");
                        if (content.contains(targetBaseName)) {
                            links.add(GraphLink.builder()
                                    .source(relativeSrc)
                                    .target(otherNode.getId())
                                    .relationship("DEPENDS_ON")
                                    .weight(1.0)
                                    .build());
                        }
                    }
                }
            }
            
            return GraphResponse.builder()
                    .success(true)
                    .message("Graph constructed from local filesystem (offline fallback)")
                    .nodes(nodes)
                    .links(links)
                    .build();
            
        } catch (Exception ex) {
            log.error("Failed to build fallback graph from filesystem", ex);
            return GraphResponse.builder()
                    .success(false)
                    .message("Unable to inspect repository graph")
                    .nodes(Collections.emptyList())
                    .links(Collections.emptyList())
                    .build();
        }
    }

    private List<GraphNode> parseNodes(List<Map<String, Object>> rawNodes) {
        Map<String, GraphNode> uniqueNodes = new LinkedHashMap<>();

        for (Map<String, Object> raw : rawNodes) {
            String id = extractString(raw, "id", "nodeId", "entityId", "name");
            if (id == null) continue; // Skip invalid nodes

            String label = extractString(raw, "label", "name", "id");
            String type = extractString(raw, "type", "nodeType", "entityType");
            String path = extractString(raw, "path", "filePath", "uri");

            if (type == null) type = "UNKNOWN";

            GraphNode node = GraphNode.builder()
                    .id(id)
                    .label(label)
                    .type(type.toUpperCase())
                    .path(path)
                    .metadata(raw)
                    .build();

            uniqueNodes.putIfAbsent(id, node);
        }

        return new ArrayList<>(uniqueNodes.values());
    }

    private List<GraphLink> parseLinks(List<Map<String, Object>> rawEdges) {
        Set<String> seenLinks = new HashSet<>();
        List<GraphLink> uniqueLinks = new ArrayList<>();

        for (Map<String, Object> raw : rawEdges) {
            String source = extractString(raw, "source", "from", "sourceId");
            String target = extractString(raw, "target", "to", "targetId");
            String relationship = extractString(raw, "relationship", "type", "edgeType", "label");

            if (source == null || target == null || source.equals(target)) {
                continue; // Skip invalid or self-referential links
            }

            if (relationship == null) relationship = "RELATES_TO";
            
            Double weight = null;
            if (raw.containsKey("weight") && raw.get("weight") instanceof Number num) {
                weight = num.doubleValue();
            }

            // Deduplicate exact edges
            String linkKey = source + "|" + relationship + "|" + target;
            if (seenLinks.add(linkKey)) {
                uniqueLinks.add(GraphLink.builder()
                        .source(source)
                        .target(target)
                        .relationship(relationship.toUpperCase())
                        .weight(weight)
                        .build());
            }
        }

        return uniqueLinks;
    }

    private String extractString(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            if (map.containsKey(key) && map.get(key) != null) {
                return String.valueOf(map.get(key));
            }
        }
        return null;
    }
}

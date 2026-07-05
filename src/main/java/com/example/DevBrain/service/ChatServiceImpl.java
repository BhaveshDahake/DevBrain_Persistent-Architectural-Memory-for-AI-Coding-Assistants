package com.example.DevBrain.service;

import com.example.DevBrain.config.DatasetProperties;
import com.example.DevBrain.config.DemoInitializer;
import com.example.DevBrain.dto.ChatResponse;
import com.example.DevBrain.dto.SearchResult;
import com.example.DevBrain.exception.DatasetNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final DatasetProperties datasetProperties;
    private final CogneeClientService cogneeClientService;
    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT_TEMPLATE = """
            You are an elite software architect and repository analysis assistant.

            You MUST answer ONLY using the provided context memory.

            Rules:
            1. Use the provided context memory below to answer the user's question.
            2. If the answer cannot be found in the context, state clearly:
               "I do not know based on the provided repository context."
            3. Do not invent files, classes, methods, APIs, dependencies, or behavior.
            4. Cite referenced files when possible.
            5. Prefer concise technical explanations.
            6. If context conflicts, explain uncertainty.
            7. Output valid Markdown.

            Context Memory:
            {context}
            """;

    @Override
    public ChatResponse askQuestion(String question, String datasetName) {
        log.info("Processing chat request for dataset: {}", datasetName);
        long startTime = System.currentTimeMillis();

        // 0. Auto-initialize demo repo if missing on disk (e.g. after Reset Context)
        if ("devbrain-demo-repo".equals(datasetName)) {
            DemoInitializer.initializeDemoRepo(datasetProperties.getWorkspace(), datasetName);
        }

        // 1. Validate dataset exists on disk before querying AI layer
        Path workspacePath = Paths.get(datasetProperties.getWorkspace(), datasetName).toAbsolutePath().normalize();
        if (!Files.exists(workspacePath)) {
            log.error("Dataset not found: {}", datasetName);
            throw new DatasetNotFoundException("Dataset '" + datasetName + "' was not found.");
        }

        // 2. Query Knowledge Graph (Cognee)
        SearchResult searchResult = cogneeClientService.searchRepository(question, datasetName);

        // 3. Extract Memory Context
        String contextMemory = buildContextMemory(searchResult);

        // 4. Generate Final Answer
        String aiResponse;
        if (contextMemory.isBlank()) {
            aiResponse = "I do not know based on the provided repository context.";
            log.info("Context empty. Skipping LLM execution.");
        } else {
            aiResponse = executeLlm(question, contextMemory);
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("Chat request completed in {}ms. Found {} matches.", duration, searchResult.getContexts() != null ? searchResult.getContexts().size() : 0);

        // 5. Construct and return DTO
        return ChatResponse.builder()
                .success(true)
                .answer(aiResponse)
                .datasetName(datasetName)
                .timestamp(Instant.now())
                .build();
    }

    private String buildContextMemory(SearchResult searchResult) {
        if (searchResult == null || searchResult.getContexts() == null || searchResult.getContexts().isEmpty()) {
            return "";
        }
        
        // Deduplicate and merge, formatting cleanly for the LLM
        return searchResult.getContexts().stream()
                .map(ctx -> {
                    StringBuilder sb = new StringBuilder();
                    if (ctx.getFile() != null) sb.append("File: ").append(ctx.getFile()).append("\n");
                    if (ctx.getFunction() != null) sb.append("Function: ").append(ctx.getFunction()).append("\n");
                    if (ctx.getSummary() != null) sb.append("Summary: \n").append(ctx.getSummary()).append("\n");
                    return sb.toString();
                })
                .collect(Collectors.joining("\n---\n"));
    }

    private String executeLlm(String question, String contextMemory) {
        log.info("Executing LLM generation via Spring AI...");
        return chatClient.prompt()
                .system(sys -> sys.text(SYSTEM_PROMPT_TEMPLATE).param("context", contextMemory))
                .user(question)
                .call()
                .content();
    }
}

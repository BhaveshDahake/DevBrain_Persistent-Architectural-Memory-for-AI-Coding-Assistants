package com.example.DevBrain.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Component
@RequiredArgsConstructor
public class DemoInitializer implements ApplicationRunner {

    private final DatasetProperties datasetProperties;

    @Override
    public void run(ApplicationArguments args) {
        initializeDemoRepo(datasetProperties.getWorkspace(), "devbrain-demo-repo");
    }

    public static void initializeDemoRepo(String workspaceRoot, String datasetName) {
        Path cleanPath = Paths.get(workspaceRoot, datasetName, "clean").toAbsolutePath().normalize();

        if (Files.exists(cleanPath)) {
            return;
        }

        log.info("Initializing demo repository at {}...", cleanPath);
        try {
            Files.createDirectories(cleanPath);

            // File 1: RateLimitConfig.java
            writeSampleFile(cleanPath, "src/main/java/com/example/DevBrain/config/RateLimitConfig.java", 
                "package com.example.DevBrain.config;\n\n" +
                "import io.github.bucket4j.Bandwidth;\n" +
                "import io.github.bucket4j.Bucket;\n" +
                "import io.github.bucket4j.Refill;\n" +
                "import org.springframework.context.annotation.Configuration;\n" +
                "import java.time.Duration;\n\n" +
                "@Configuration\n" +
                "public class RateLimitConfig {\n" +
                "    // Limits chat requests to 30 per minute\n" +
                "    public Bucket chatBucket() {\n" +
                "        return Bucket.builder()\n" +
                "            .addLimit(Bandwidth.classic(30, Refill.intervally(30, Duration.ofMinutes(1))))\n" +
                "            .build();\n" +
                "    }\n" +
                "}"
            );

            // File 2: CogneeClientService.java
            writeSampleFile(cleanPath, "src/main/java/com/example/DevBrain/service/CogneeClientService.java",
                "package com.example.DevBrain.service;\n\n" +
                "import org.springframework.stereotype.Service;\n" +
                "import org.springframework.web.reactive.function.client.WebClient;\n\n" +
                "@Service\n" +
                "public class CogneeClientService {\n" +
                "    private final WebClient webClient;\n\n" +
                "    public CogneeClientService(WebClient.Builder builder) {\n" +
                "        this.webClient = builder.baseUrl(\"https://api.cognee.ai\").build();\n" +
                "    }\n\n" +
                "    public void rememberRepository(String datasetName, String folderPath) {\n" +
                "        // Uploads AST nodes to Cognee memory graph\n" +
                "    }\n" +
                "}"
            );

            // File 3: ChatServiceImpl.java
            writeSampleFile(cleanPath, "src/main/java/com/example/DevBrain/service/ChatServiceImpl.java",
                "package com.example.DevBrain.service;\n\n" +
                "import org.springframework.ai.chat.client.ChatClient;\n" +
                "import org.springframework.stereotype.Service;\n\n" +
                "@Service\n" +
                "public class ChatServiceImpl {\n" +
                "    private final ChatClient chatClient;\n\n" +
                "    public ChatServiceImpl(ChatClient chatClient) {\n" +
                "        this.chatClient = chatClient;\n" +
                "    }\n\n" +
                "    public String askQuestion(String question, String context) {\n" +
                "        // Orchestrates RAG via Spring AI\n" +
                "        return \"Mock Answer\";\n" +
                "    }\n" +
                "}"
            );

            // File 4: RepositoryGraph.jsx
            writeSampleFile(cleanPath, "frontend/src/components/RepositoryGraph.jsx",
                "import React from 'react';\n" +
                "import ForceGraph2D from 'react-force-graph-2d';\n\n" +
                "const RepositoryGraph = ({ graphData }) => {\n" +
                "    return (\n" +
                "        <ForceGraph2D graphData={graphData} />\n" +
                "    );\n" +
                "};\n" +
                "export default RepositoryGraph;"
            );

            log.info("Demo repository initialized successfully.");

        } catch (IOException e) {
            log.error("Failed to initialize demo repository", e);
        }
    }

    private static void writeSampleFile(Path root, String relativePath, String content) throws IOException {
        Path target = root.resolve(relativePath);
        Files.createDirectories(target.getParent());
        Files.writeString(target, content);
    }
}

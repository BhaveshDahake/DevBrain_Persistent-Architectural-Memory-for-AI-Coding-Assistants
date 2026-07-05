package com.example.DevBrain.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.nio.file.Files;
import java.nio.file.Path;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@Disabled("Integration test requires WireMock runtime; disabled in CI")
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CogneeAsyncIntegrationTest {

    static WireMockServer wm;
    static {
        wm = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wm.start();
        WireMock.configureFor("localhost", wm.port());
    }

    @Autowired
    private CogneeClientService cogneeClientService;

    @BeforeAll
    public void beforeAll() {
        // WireMock started in static initializer
    }

    @AfterAll
    public void stopWiremock() {
        if (wm != null) wm.stop();
    }

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("cognee.api.baseUrl", () -> "http://localhost:" + wm.port());
    }

    @Test
    public void rememberPollAndSearchFlow() throws Exception {
        // Stub remember to return running with dataset id
        wm.stubFor(post(urlEqualTo("/api/v1/remember"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBody("{\"status\":\"running\", \"dataset_id\":\"ds-1\"}")
                        .withStatus(200)));

        // Stub dataset status: return completed immediately for test simplicity
        wm.stubFor(get(urlPathEqualTo("/api/v1/datasets/status"))
            .withQueryParam("dataset", equalTo("ds-1"))
            .withQueryParam("pipeline", equalTo("cognify_pipeline"))
            .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("{\"ds-1\":\"completed\"}").withStatus(200)));

        // Stub dataset list for resolveDatasetId
        wm.stubFor(get(urlEqualTo("/api/v1/datasets/"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("[{\"id\":\"ds-1\",\"name\":\"test-dataset\"}]").withStatus(200)));

        // Stub search to return a simple result
        wm.stubFor(post(urlEqualTo("/api/v1/search"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("[{\"dataset_name\":\"test-dataset\",\"search_result\":[\"This is the answer\"]}]").withStatus(200)));

        // Create a temporary clean folder with one file
        Path temp = Files.createTempDirectory("cognee-test-");
        Path clean = temp.resolve("clean");
        Files.createDirectories(clean);
        Path sample = clean.resolve("README.md");
        Files.writeString(sample, "Sample content for test");

        // Call rememberRepository which should poll until completed
        var kgResponse = cogneeClientService.rememberRepository("test-dataset", clean.toString());
        Assertions.assertTrue(kgResponse.isSuccess(), "Knowledge graph ingestion should succeed");

        // Now perform a search
        var searchResult = cogneeClientService.searchRepository("What is this?", "test-dataset");
        Assertions.assertNotNull(searchResult);
        Assertions.assertFalse(searchResult.isProcessing(), "Search should not be processing after indexing completes");
        Assertions.assertTrue(searchResult.getAnswer().contains("This is the answer"));
    }
}

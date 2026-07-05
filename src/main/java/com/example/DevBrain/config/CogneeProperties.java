package com.example.DevBrain.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "cognee")
public class CogneeProperties {

    private Api api = new Api();
    private Request request = new Request();
    private Batch batch = new Batch();
    private Fallback fallback = new Fallback();
    private DataSize maxFileSize = DataSize.ofMegabytes(5);
    private boolean mock = false;

    public Api getApi() { return api; }
    public void setApi(Api api) { this.api = api; }

    public Request getRequest() { return request; }
    public void setRequest(Request request) { this.request = request; }

    public Batch getBatch() { return batch; }
    public void setBatch(Batch batch) { this.batch = batch; }

    public Fallback getFallback() { return fallback; }
    public void setFallback(Fallback fallback) { this.fallback = fallback; }

    public DataSize getMaxFileSize() { return maxFileSize; }
    public void setMaxFileSize(DataSize maxFileSize) { this.maxFileSize = maxFileSize; }

    public boolean isMock() { return mock; }
    public void setMock(boolean mock) { this.mock = mock; }

    public static class Api {
        private String baseUrl;
        private String key;
        private String rememberEndpoint = "/api/v1/remember";
        private String searchEndpoint = "/api/v1/search";
        private String improveEndpoint = "/api/v1/remember/entry";
        private String forgetEndpoint = "/api/v1/datasets";
        private String graphEndpoint = "/api/v1/search";
        private boolean mock = false;

        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }

        public String getRememberEndpoint() { return rememberEndpoint; }
        public void setRememberEndpoint(String rememberEndpoint) { this.rememberEndpoint = rememberEndpoint; }

        public String getSearchEndpoint() { return searchEndpoint; }
        public void setSearchEndpoint(String searchEndpoint) { this.searchEndpoint = searchEndpoint; }

        public String getImproveEndpoint() { return improveEndpoint; }
        public void setImproveEndpoint(String improveEndpoint) { this.improveEndpoint = improveEndpoint; }

        public String getForgetEndpoint() { return forgetEndpoint; }
        public void setForgetEndpoint(String forgetEndpoint) { this.forgetEndpoint = forgetEndpoint; }

        public String getGraphEndpoint() { return graphEndpoint; }
        public void setGraphEndpoint(String graphEndpoint) { this.graphEndpoint = graphEndpoint; }

        public boolean isMock() { return mock; }
        public void setMock(boolean mock) { this.mock = mock; }
    }

    public static class Request {
        private Duration timeout = Duration.ofSeconds(60);
        public Duration getTimeout() { return timeout; }
        public void setTimeout(Duration timeout) { this.timeout = timeout; }
    }

    public static class Batch {
        private int size = 50;
        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }
    }

    public static class Fallback {
        private int failureThreshold = 3;
        private Duration cooldown = Duration.ofMinutes(1);
        private Duration timeout = Duration.ofSeconds(60);

        public int getFailureThreshold() { return failureThreshold; }
        public void setFailureThreshold(int failureThreshold) { this.failureThreshold = failureThreshold; }

        public Duration getCooldown() { return cooldown; }
        public void setCooldown(Duration cooldown) { this.cooldown = cooldown; }

        public Duration getTimeout() { return timeout; }
        public void setTimeout(Duration timeout) { this.timeout = timeout; }
    }
}

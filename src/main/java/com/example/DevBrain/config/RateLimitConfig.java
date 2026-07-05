package com.example.DevBrain.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class RateLimitConfig {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    @Value("${app.rate-limit.chat:30/min}")
    private String chatLimit;

    @Value("${app.rate-limit.upload:5/hour}")
    private String uploadLimit;

    @Value("${app.rate-limit.memory:20/hour}")
    private String memoryLimit;

    public Bucket resolveBucket(String apiKey, String endpointType) {
        return cache.computeIfAbsent(apiKey + "-" + endpointType, this::newBucket);
    }

    private Bucket newBucket(String key) {
        String limitConfig;
        if (key.endsWith("-chat")) limitConfig = chatLimit;
        else if (key.endsWith("-upload")) limitConfig = uploadLimit;
        else if (key.endsWith("-memory")) limitConfig = memoryLimit;
        else limitConfig = "100/min"; // Default fallback

        String[] parts = limitConfig.split("/");
        long capacity = Long.parseLong(parts[0]);
        Duration duration = parts[1].equals("min") ? Duration.ofMinutes(1) : Duration.ofHours(1);

        Bandwidth limit = Bandwidth.builder()
                .capacity(capacity)
                .refillGreedy(capacity, duration)
                .build();
        return Bucket.builder().addLimit(limit).build();
    }
}

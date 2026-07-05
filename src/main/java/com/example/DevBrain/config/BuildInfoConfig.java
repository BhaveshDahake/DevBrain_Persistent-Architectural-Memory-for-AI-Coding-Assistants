package com.example.DevBrain.config;

import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.util.Properties;

@Configuration
public class BuildInfoConfig {

    @Bean
    public BuildProperties buildProperties() {
        Properties properties = new Properties();
        properties.setProperty("version", "0.0.1-SNAPSHOT");
        properties.setProperty("artifact", "DevBrain");
        properties.setProperty("group", "com.example");
        properties.setProperty("name", "DevBrain");
        properties.setProperty("time", Instant.now().toString());
        return new BuildProperties(properties);
    }
}

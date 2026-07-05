package com.example.DevBrain;

import com.example.DevBrain.repository.FeedbackRepository;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestRepositoryConfig {

    @Bean
    public FeedbackRepository feedbackRepository() {
        return Mockito.mock(FeedbackRepository.class);
    }
}

package com.example.DevBrain.service;

import com.example.DevBrain.dto.EnrichmentResponse;
import com.example.DevBrain.dto.ImproveMemoryRequest;
import com.example.DevBrain.dto.ImproveMemoryResponse;
import com.example.DevBrain.entity.FeedbackEvent;
import com.example.DevBrain.repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemoryImprovementServiceImpl implements MemoryImprovementService {

    private final FeedbackRepository feedbackRepository;
    private final CogneeClientService cogneeClientService;

    @Override
    @Transactional
    public ImproveMemoryResponse improve(ImproveMemoryRequest request) {
        log.info("Received memory feedback for dataset: {}, messageId: {}, positive: {}", 
                request.getDatasetName(), request.getMessageId(), request.getPositive());

        // 1. Deduplication Check
        Optional<FeedbackEvent> existingFeedback = feedbackRepository.findByMessageIdAndDatasetName(request.getMessageId(), request.getDatasetName());
        if (existingFeedback.isPresent()) {
            log.info("Feedback already recorded for message {}", request.getMessageId());
            return ImproveMemoryResponse.builder()
                    .success(true)
                    .message("Feedback was already recorded")
                    .enrichmentTriggered(false)
                    .build();
        }

        // 2. Persist Feedback
        FeedbackEvent event = FeedbackEvent.builder()
                .messageId(request.getMessageId())
                .datasetName(request.getDatasetName())
                .positive(request.getPositive())
                .timestamp(Instant.now())
                .enrichmentStatus("PENDING")
                .build();
        
        feedbackRepository.save(event);

        // 3. Determine Enrichment Eligibility
        if (Boolean.TRUE.equals(request.getPositive())) {
            log.info("Positive feedback triggers async knowledge graph enrichment for message {}", request.getMessageId());
            
            try {
                // Using try-catch to not fail the main feedback loop if enrichment dispatch fails immediately
                EnrichmentResponse enrichmentResponse = cogneeClientService.improveMemory(request.getDatasetName(), request.getMessageId());
                event.setEnrichmentStatus(enrichmentResponse != null && Boolean.TRUE.equals(enrichmentResponse.getSuccess()) ? "TRIGGERED" : "FAILED_TRIGGER");
            } catch (Exception e) {
                log.error("Failed to trigger enrichment process for message {}", request.getMessageId(), e);
                event.setEnrichmentStatus("ERROR");
            }
            
            return ImproveMemoryResponse.builder()
                    .success(true)
                    .message("Repository memory enrichment started")
                    .enrichmentTriggered(true)
                    .build();
        } else {
            log.info("Negative feedback recorded. No enrichment triggered for message {}", request.getMessageId());
            event.setEnrichmentStatus("NOT_ELIGIBLE");
            return ImproveMemoryResponse.builder()
                    .success(true)
                    .message("Feedback recorded")
                    .enrichmentTriggered(false)
                    .build();
        }
    }
}

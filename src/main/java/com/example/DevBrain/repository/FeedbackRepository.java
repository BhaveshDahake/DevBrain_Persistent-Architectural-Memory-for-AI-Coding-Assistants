package com.example.DevBrain.repository;

import com.example.DevBrain.entity.FeedbackEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<FeedbackEvent, Long> {
    Optional<FeedbackEvent> findByMessageIdAndDatasetName(String messageId, String datasetName);
}

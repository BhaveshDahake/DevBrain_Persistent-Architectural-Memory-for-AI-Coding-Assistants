package com.example.DevBrain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "feedback_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String messageId;

    @Column(nullable = false)
    private String datasetName;

    @Column(nullable = false)
    private Boolean positive;

    @Column(nullable = false)
    private Instant timestamp;

    @Column
    private String enrichmentStatus;
}

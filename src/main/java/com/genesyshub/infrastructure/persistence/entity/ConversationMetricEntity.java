package com.genesyshub.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "conversation_metrics",
        indexes = {
                @Index(name = "idx_cm_queue_id",   columnList = "queue_id"),
                @Index(name = "idx_cm_start_time", columnList = "start_time")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class ConversationMetricEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "conversation_id", nullable = false, unique = true)
    private String conversationId;

    @Column(name = "queue_id")
    private String queueId;

    @Column(name = "agent_id")
    private String agentId;

    @Column(name = "media_type")
    private String mediaType;

    @Column(name = "direction")
    private String direction;

    @Column(name = "start_time")
    private Instant startTime;

    @Column(name = "end_time")
    private Instant endTime;

    @Column(name = "handle_time_seconds")
    private Long handleTimeSeconds;

    @Column(name = "abandoned", nullable = false)
    private boolean abandoned;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}

package com.agrimarket.order.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "outbox_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "aggregate_type")
    private String aggregateType;

    @Column(name = "aggregate_id")
    private String aggregateId;

    private String type;

    @Column(name = "routing_key")
    private String routingKey;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    private boolean processed;
}

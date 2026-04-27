package com.nivasafinance.features.transaction.entity;

import com.nivasafinance.features.transaction.enums.TransactionStatus;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "n_transaction_event")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "identifier", nullable = false, unique = true)
    private UUID identifier;

    @Column(name = "transaction_id", nullable = false)
    private Long transactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 100)
    private TransactionStatus eventType;

    @Column(name = "actor_username", nullable = false)
    private String actorUsername;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payment_details", columnDefinition = "jsonb")
    private Map<String, Object> paymentDetails;

    @Column(name = "event_timestamp", nullable = false)
    private LocalDateTime eventTimestamp;

    @Column(name = "transaction_payment_id")
    private Long transactionPaymentId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

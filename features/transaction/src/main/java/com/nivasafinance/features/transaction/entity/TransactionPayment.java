package com.nivasafinance.features.transaction.entity;

import com.nivasafinance.features.transaction.enums.PaymentMode;
import com.nivasafinance.features.transaction.enums.PaymentStatus;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "n_transaction_payment")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "identifier", nullable = false, unique = true)
    private UUID identifier;

    @Column(name = "transaction_id", nullable = false)
    private Long transactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode", nullable = false, length = 100)
    private PaymentMode paymentMode;

    @Column(name = "external_reference")
    private String externalReference;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 100)
    private PaymentStatus paymentStatus;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(name = "recorded_by", nullable = false)
    private String recordedBy;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payment_data", columnDefinition = "jsonb")
    private Map<String, Object> paymentData;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Version
    @Column(name = "version")
    private Long version = 0L;
}

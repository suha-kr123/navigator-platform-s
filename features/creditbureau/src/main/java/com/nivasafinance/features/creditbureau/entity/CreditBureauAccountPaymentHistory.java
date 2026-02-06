package com.nivasafinance.features.creditbureau.entity;

import com.nivasafinance.common.audit.IdentifiableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "n_cb_account_payment_history")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class CreditBureauAccountPaymentHistory extends IdentifiableEntity {

    @Column(name = "identifier", nullable = false, unique = true)
    @Builder.Default
    private UUID identifier = UUID.randomUUID();

    @Column(name = "tradeline_id", nullable = false)
    private Long tradelineId;

    @Column(name = "history_type", length = 50)
    private String historyType;

    @Column(name = "month_year", length = 10)
    private String monthYear;

    @Column(name = "history_value", length = 20)
    private String historyValue;
}

package com.nivasafinance.features.transaction.dto;

import com.nivasafinance.features.transaction.enums.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDashboardFilters {

    private TransactionStatus status;
}

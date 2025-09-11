package com.nivasafinance.features.payment.enum

enum class PaymentType {
    // Commission and Fee Payments
    COMMISSION,
    ADVISOR_FEE,
    PROCESSING_FEE,
    SERVICE_FEE,
    CONSULTATION_FEE,

    // Loan and Credit Payments
    LOAN_EMI,
    PRINCIPAL_PAYMENT,
    INTEREST_PAYMENT,
    PENALTY_PAYMENT,
    PREPAYMENT,

    // Insurance Payments
    PREMIUM_PAYMENT,
    CLAIM_PAYMENT,
    REFUND_PAYMENT,

    // Investment Payments
    INVESTMENT_PAYMENT,
    DIVIDEND_PAYMENT,
    REDEMPTION_PAYMENT,

    // General Payments
    SALARY_PAYMENT,
    BONUS_PAYMENT,
    REIMBURSEMENT,
    REFUND,
    DEPOSIT,
    WITHDRAWAL,

    // Transaction Types
    TRANSFER,
    PAYMENT_RECEIVED,
    PAYMENT_MADE,

    // Other
    CUSTOM
}

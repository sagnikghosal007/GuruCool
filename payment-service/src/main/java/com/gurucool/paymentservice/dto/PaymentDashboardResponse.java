package com.gurucool.paymentservice.dto;

import java.math.BigDecimal;

public record PaymentDashboardResponse(
    BigDecimal totalRevenue,
    BigDecimal totalRefunds,
    long totalTransactions,
    long failedTransactions,
    BigDecimal netRevenue,
    String note
) {}

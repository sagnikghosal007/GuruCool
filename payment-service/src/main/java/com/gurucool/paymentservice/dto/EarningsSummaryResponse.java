package com.gurucool.paymentservice.dto;

import java.math.BigDecimal;

public record EarningsSummaryResponse(
    BigDecimal totalEarned,
    BigDecimal pendingPayout,
    long completedSessions,
    String currency
) {}

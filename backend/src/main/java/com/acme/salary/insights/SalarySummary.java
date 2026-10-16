package com.acme.salary.insights;

import java.math.BigDecimal;

/** Headline numbers for the dashboard. Money values are annual, in USD. */
public record SalarySummary(
        long headcount,
        BigDecimal totalPayrollUsd,
        BigDecimal medianUsd,
        BigDecimal averageUsd,
        int countries,
        int departments) {
}

package com.acme.salary.insights;

import java.math.BigDecimal;

/**
 * Salary distribution for one group (e.g. one country). USD figures allow comparison across groups.
 * {@code localCurrency} and {@code medianLocal} are present only when every employee in the group
 * is paid in the same currency, because a "local median" of mixed currencies would be meaningless.
 */
public record GroupStats(
        String group,
        long headcount,
        BigDecimal minUsd,
        BigDecimal p25Usd,
        BigDecimal medianUsd,
        BigDecimal averageUsd,
        BigDecimal p75Usd,
        BigDecimal maxUsd,
        String localCurrency,
        BigDecimal medianLocal) {
}

package com.acme.salary.insights;

import java.math.BigDecimal;

/**
 * An employee whose salary is far from the median of their peers
 * (same job title, country and currency). Amounts are in the employee's local currency.
 */
public record Outlier(
        long id,
        String employeeCode,
        String fullName,
        String jobTitle,
        String department,
        String countryCode,
        String currencyCode,
        BigDecimal annualSalary,
        BigDecimal peerMedian,
        long peerCount,
        BigDecimal deviationPercent) {
}

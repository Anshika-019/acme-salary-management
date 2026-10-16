package com.acme.salary.seed;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Plain data produced by {@link EmployeeGenerator}; independent of JPA so it is trivially testable. */
public record GeneratedEmployee(
        String employeeCode,
        String fullName,
        String email,
        String jobTitle,
        String department,
        String countryCode,
        String currencyCode,
        BigDecimal annualSalary,
        LocalDate hireDate) {
}

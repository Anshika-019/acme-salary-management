package com.acme.salary.employee;

import com.acme.salary.currency.ExchangeRate;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * API view of an employee. Includes the salary normalised to USD so salaries in
 * different currencies can be compared at a glance.
 */
public record EmployeeResponse(
        Long id,
        String employeeCode,
        String fullName,
        String email,
        String jobTitle,
        String department,
        String countryCode,
        String currencyCode,
        BigDecimal annualSalary,
        BigDecimal annualSalaryUsd,
        LocalDate hireDate,
        Long version) {

    static EmployeeResponse from(Employee e, ExchangeRate rate) {
        return new EmployeeResponse(
                e.getId(),
                e.getEmployeeCode(),
                e.getFullName(),
                e.getEmail(),
                e.getJobTitle(),
                e.getDepartment(),
                e.getCountryCode(),
                e.getCurrencyCode(),
                e.getAnnualSalary(),
                rate == null ? null : rate.toUsd(e.getAnnualSalary()),
                e.getHireDate(),
                e.getVersion());
    }
}

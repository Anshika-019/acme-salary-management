package com.acme.salary.employee;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;

/**
 * Payload for creating or updating an employee.
 * {@code version} is ignored on create and required on update (optimistic locking).
 */
public record EmployeeRequest(
        @NotBlank @Size(max = 20)
        @Pattern(regexp = "\\s*[A-Za-z]{3}-\\d{5,}\\s*", message = "must look like EMP-00001")
        String employeeCode,

        @NotBlank @Size(max = 200) String fullName,

        @NotBlank @Email @Size(max = 254) String email,

        @NotBlank @Size(max = 100) String jobTitle,

        @NotBlank @Size(max = 100) String department,

        @NotBlank @Pattern(regexp = "\\s*[A-Za-z]{2}\\s*", message = "must be a 2-letter ISO country code")
        String countryCode,

        @NotBlank @Pattern(regexp = "\\s*[A-Za-z]{3}\\s*", message = "must be a 3-letter ISO currency code")
        String currencyCode,

        @NotNull @Positive @Digits(integer = 12, fraction = 2) BigDecimal annualSalary,

        @NotNull @PastOrPresent LocalDate hireDate,

        Long version) {

    /** Trims text and applies canonical casing so "  us " and "US" are treated the same. */
    EmployeeRequest normalized() {
        return new EmployeeRequest(
                employeeCode.trim().toUpperCase(Locale.ROOT),
                fullName.trim(),
                email.trim().toLowerCase(Locale.ROOT),
                jobTitle.trim(),
                department.trim(),
                countryCode.trim().toUpperCase(Locale.ROOT),
                currencyCode.trim().toUpperCase(Locale.ROOT),
                annualSalary,
                hireDate,
                version);
    }
}

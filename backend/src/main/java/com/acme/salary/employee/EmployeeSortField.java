package com.acme.salary.employee;

import com.acme.salary.common.BadRequestException;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Whitelist of sortable columns. Accepting arbitrary property names from the client would
 * leak internal field names and turn typos into 500 errors.
 */
public enum EmployeeSortField {
    FULL_NAME("fullName"),
    EMPLOYEE_CODE("employeeCode"),
    JOB_TITLE("jobTitle"),
    DEPARTMENT("department"),
    COUNTRY("countryCode"),
    ANNUAL_SALARY("annualSalary"),
    HIRE_DATE("hireDate");

    private final String property;

    EmployeeSortField(String property) {
        this.property = property;
    }

    public String property() {
        return property;
    }

    public static EmployeeSortField fromParam(String value) {
        return Arrays.stream(values())
                .filter(field -> field.property.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Cannot sort by '" + value + "'. Allowed: "
                        + Arrays.stream(values()).map(EmployeeSortField::property).collect(Collectors.joining(", "))));
    }
}

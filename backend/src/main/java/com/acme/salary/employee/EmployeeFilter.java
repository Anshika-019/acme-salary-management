package com.acme.salary.employee;

/** Optional criteria for the employee directory; null or blank values are ignored. */
public record EmployeeFilter(String search, String countryCode, String department, String jobTitle) {

    public static EmployeeFilter none() {
        return new EmployeeFilter(null, null, null, null);
    }
}

package com.acme.salary.employee;

import java.util.List;

/** Distinct values that populate the directory's filter dropdowns. */
public record FilterOptions(List<String> countries, List<String> departments, List<String> jobTitles) {
}

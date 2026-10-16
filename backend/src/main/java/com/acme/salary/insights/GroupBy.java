package com.acme.salary.insights;

import com.acme.salary.common.BadRequestException;

import java.util.Arrays;

/**
 * Dimensions salaries can be grouped by. Each maps to a fixed SQL column, so the
 * client never supplies raw SQL. This is what keeps dynamic GROUP BY safe from injection.
 */
public enum GroupBy {
    COUNTRY("country", "e.country_code"),
    DEPARTMENT("department", "e.department"),
    JOB_TITLE("jobTitle", "e.job_title");

    private final String param;
    private final String column;

    GroupBy(String param, String column) {
        this.param = param;
        this.column = column;
    }

    String column() {
        return column;
    }

    public static GroupBy fromParam(String value) {
        return Arrays.stream(values())
                .filter(g -> g.param.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new BadRequestException(
                        "groupBy must be one of: country, department, jobTitle"));
    }
}

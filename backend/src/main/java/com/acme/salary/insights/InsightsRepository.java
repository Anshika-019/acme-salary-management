package com.acme.salary.insights;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Analytics queries in plain SQL. Aggregation happens inside Postgres
 * ({@code percentile_cont} gives true medians) instead of loading 10,000 rows into Java.
 * Salaries are converted to USD in the query by joining the exchange-rate snapshot.
 */
@Repository
public class InsightsRepository {

    private static final String USD = "(e.annual_salary * r.rate_to_usd)";

    private final NamedParameterJdbcTemplate jdbc;

    public InsightsRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public SalarySummary summary(InsightsFilter filter) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        String sql = """
                SELECT COUNT(*)                                                         AS headcount,
                       ROUND(COALESCE(SUM(%1$s), 0), 2)                                 AS total_usd,
                       ROUND(CAST(percentile_cont(0.5) WITHIN GROUP (ORDER BY %1$s) AS numeric), 2) AS median_usd,
                       ROUND(AVG(%1$s), 2)                                              AS average_usd,
                       COUNT(DISTINCT e.country_code)                                   AS countries,
                       COUNT(DISTINCT e.department)                                     AS departments
                FROM employee e
                JOIN exchange_rate r ON r.currency_code = e.currency_code
                %2$s
                """.formatted(USD, where(filter, params));

        return jdbc.queryForObject(sql, params, (rs, row) -> new SalarySummary(
                rs.getLong("headcount"),
                rs.getBigDecimal("total_usd"),
                rs.getBigDecimal("median_usd"),
                rs.getBigDecimal("average_usd"),
                rs.getInt("countries"),
                rs.getInt("departments")));
    }

    public List<GroupStats> statsBy(GroupBy groupBy, InsightsFilter filter) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        String sql = """
                SELECT TRIM(%2$s)                                                                   AS group_key,
                       COUNT(*)                                                                     AS headcount,
                       ROUND(MIN(%1$s), 2)                                                          AS min_usd,
                       ROUND(CAST(percentile_cont(0.25) WITHIN GROUP (ORDER BY %1$s) AS numeric), 2) AS p25_usd,
                       ROUND(CAST(percentile_cont(0.50) WITHIN GROUP (ORDER BY %1$s) AS numeric), 2) AS median_usd,
                       ROUND(AVG(%1$s), 2)                                                          AS average_usd,
                       ROUND(CAST(percentile_cont(0.75) WITHIN GROUP (ORDER BY %1$s) AS numeric), 2) AS p75_usd,
                       ROUND(MAX(%1$s), 2)                                                          AS max_usd,
                       CASE WHEN COUNT(DISTINCT e.currency_code) = 1
                            THEN TRIM(MIN(e.currency_code)) END                                     AS local_currency,
                       CASE WHEN COUNT(DISTINCT e.currency_code) = 1
                            THEN ROUND(CAST(percentile_cont(0.5) WITHIN GROUP (ORDER BY e.annual_salary) AS numeric), 2)
                            END                                                                     AS median_local
                FROM employee e
                JOIN exchange_rate r ON r.currency_code = e.currency_code
                %3$s
                GROUP BY %2$s
                ORDER BY median_usd DESC, group_key
                """.formatted(USD, groupBy.column(), where(filter, params));

        return jdbc.query(sql, params, (rs, row) -> new GroupStats(
                rs.getString("group_key"),
                rs.getLong("headcount"),
                rs.getBigDecimal("min_usd"),
                rs.getBigDecimal("p25_usd"),
                rs.getBigDecimal("median_usd"),
                rs.getBigDecimal("average_usd"),
                rs.getBigDecimal("p75_usd"),
                rs.getBigDecimal("max_usd"),
                rs.getString("local_currency"),
                rs.getBigDecimal("median_local")));
    }

    /**
     * Peer medians are computed over the whole organisation, then the filter narrows which
     * employees are reported. Filtering by department must not shrink someone's peer group.
     * Comparison is in local currency, so exchange rates cannot create false outliers.
     */
    public OutlierReport outliers(InsightsFilter filter, BigDecimal threshold, int minPeerGroupSize, int limit) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("threshold", threshold)
                .addValue("minPeers", minPeerGroupSize)
                .addValue("limit", limit);
        String sql = """
                WITH peers AS (
                    SELECT job_title, country_code, currency_code,
                           COUNT(*) AS peer_count,
                           CAST(percentile_cont(0.5) WITHIN GROUP (ORDER BY annual_salary) AS numeric) AS peer_median
                    FROM employee
                    GROUP BY job_title, country_code, currency_code
                    HAVING COUNT(*) >= :minPeers
                ),
                candidates AS (
                    SELECT e.id, e.employee_code, e.full_name, e.job_title, e.department,
                           e.country_code, e.currency_code, e.annual_salary,
                           p.peer_median, p.peer_count,
                           (e.annual_salary - p.peer_median) / p.peer_median AS deviation
                    FROM employee e
                    JOIN peers p ON p.job_title = e.job_title
                                AND p.country_code = e.country_code
                                AND p.currency_code = e.currency_code
                    %s
                )
                SELECT c.*, COUNT(*) OVER () AS total_count
                FROM candidates c
                WHERE ABS(c.deviation) >= :threshold
                ORDER BY ABS(c.deviation) DESC, c.id
                LIMIT :limit
                """.formatted(where(filter, params));

        long[] total = {0};
        List<Outlier> outliers = jdbc.query(sql, params, (rs, row) -> {
            total[0] = rs.getLong("total_count");
            return new Outlier(
                    rs.getLong("id"),
                    rs.getString("employee_code"),
                    rs.getString("full_name"),
                    rs.getString("job_title"),
                    rs.getString("department"),
                    rs.getString("country_code").trim(),
                    rs.getString("currency_code").trim(),
                    rs.getBigDecimal("annual_salary"),
                    rs.getBigDecimal("peer_median").setScale(2, java.math.RoundingMode.HALF_UP),
                    rs.getLong("peer_count"),
                    rs.getBigDecimal("deviation").movePointRight(2).setScale(1, java.math.RoundingMode.HALF_UP));
        });
        return new OutlierReport(threshold, minPeerGroupSize, total[0], outliers);
    }

    /**
     * Builds the WHERE clause from only the filters that are set. Values always travel as bind
     * parameters; this also avoids Postgres failing to infer the type of a null parameter.
     */
    private static String where(InsightsFilter filter, MapSqlParameterSource params) {
        List<String> conditions = new ArrayList<>();
        if (hasText(filter.countryCode())) {
            conditions.add("e.country_code = :country");
            params.addValue("country", filter.countryCode().trim().toUpperCase(Locale.ROOT));
        }
        if (hasText(filter.department())) {
            conditions.add("e.department = :department");
            params.addValue("department", filter.department().trim());
        }
        if (hasText(filter.jobTitle())) {
            conditions.add("e.job_title = :jobTitle");
            params.addValue("jobTitle", filter.jobTitle().trim());
        }
        return conditions.isEmpty() ? "" : "WHERE " + String.join(" AND ", conditions);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

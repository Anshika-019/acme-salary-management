package com.acme.salary.seed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import java.sql.Timestamp;
import java.time.Instant;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Loads demo employees on startup when {@code app.seed.enabled=true}.
 * <ul>
 *   <li>Idempotent: does nothing if employees already exist, so restarts are safe.</li>
 *   <li>Atomic: all rows are inserted in one transaction, so a failure never leaves half a dataset.</li>
 *   <li>Fast: uses JDBC batch inserts. JPA cannot batch inserts for IDENTITY ids.</li>
 * </ul>
 */
@Component
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(SeedProperties.class)
public class DatabaseSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);
    private static final int BATCH_SIZE = 1_000;

    private static final String INSERT_SQL = """
            INSERT INTO employee (employee_code, full_name, email, job_title, department,
                                  country_code, currency_code, annual_salary, hire_date,
                                  created_at, updated_at, version)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)
            """;

    private final JdbcTemplate jdbc;
    private final TransactionTemplate transaction;
    private final SeedProperties properties;

    public DatabaseSeeder(JdbcTemplate jdbc, TransactionTemplate transaction, SeedProperties properties) {
        this.jdbc = jdbc;
        this.transaction = transaction;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        seed(properties.count(), properties.randomSeed());
    }

    /**
     * @return number of employees inserted (0 when the table already had data)
     */
    public int seed(int count, long randomSeed) {
        Integer existing = jdbc.queryForObject("SELECT COUNT(*) FROM employee", Integer.class);
        if (existing != null && existing > 0) {
            log.info("Skipping seed: employee table already has {} rows", existing);
            return 0;
        }

        long start = System.nanoTime();
        List<GeneratedEmployee> employees = new EmployeeGenerator(loadRates()).generate(count, randomSeed);
        transaction.executeWithoutResult(status -> insertAll(employees));
        log.info("Seeded {} employees in {} ms", employees.size(), (System.nanoTime() - start) / 1_000_000);
        return employees.size();
    }

    private Map<String, BigDecimal> loadRates() {
        return jdbc.query("SELECT currency_code, rate_to_usd FROM exchange_rate",
                        (rs, row) -> Map.entry(rs.getString("currency_code").trim(), rs.getBigDecimal("rate_to_usd")))
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private void insertAll(List<GeneratedEmployee> employees) {
        Timestamp now = Timestamp.from(Instant.now());
        jdbc.batchUpdate(INSERT_SQL, employees, BATCH_SIZE, (ps, e) -> {
            ps.setString(1, e.employeeCode());
            ps.setString(2, e.fullName());
            ps.setString(3, e.email());
            ps.setString(4, e.jobTitle());
            ps.setString(5, e.department());
            ps.setString(6, e.countryCode());
            ps.setString(7, e.currencyCode());
            ps.setBigDecimal(8, e.annualSalary());
            ps.setDate(9, Date.valueOf(e.hireDate()));
            ps.setTimestamp(10, now);
            ps.setTimestamp(11, now);
        });
    }
}

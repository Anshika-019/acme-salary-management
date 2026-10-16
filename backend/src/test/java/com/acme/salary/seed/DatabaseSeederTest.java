package com.acme.salary.seed;

import com.acme.salary.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class DatabaseSeederTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private TransactionTemplate transaction;

    private DatabaseSeeder seeder;

    @BeforeEach
    void setUp() {
        seeder = new DatabaseSeeder(jdbc, transaction, new SeedProperties(true, 0, 0));
    }

    @Test
    void insertsRequestedNumberOfEmployees() {
        int inserted = seeder.seed(300, 42);

        assertThat(inserted).isEqualTo(300);
        assertThat(employeeCount()).isEqualTo(300);
    }

    @Test
    void isIdempotentWhenDataAlreadyExists() {
        seeder.seed(300, 42);

        int insertedAgain = seeder.seed(300, 42);

        assertThat(insertedAgain).isZero();
        assertThat(employeeCount()).isEqualTo(300);
    }

    private int employeeCount() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM employee", Integer.class);
    }
}

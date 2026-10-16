package com.acme.salary.seed;

import java.math.BigDecimal;
import java.util.List;

/**
 * Static reference tables that make the generated data realistic.
 * Base salaries are US-market annual figures in USD; each country applies a cost factor.
 */
final class SeedCatalog {

    private SeedCatalog() {
    }

    record Country(String code, String currency, BigDecimal costFactor, int headcountWeight) {
    }

    record Role(String department, String title, BigDecimal baseUsd, int weight) {
    }

    static final List<Country> COUNTRIES = List.of(
            new Country("IN", "INR", new BigDecimal("0.25"), 30),
            new Country("US", "USD", new BigDecimal("1.00"), 25),
            new Country("GB", "GBP", new BigDecimal("0.75"), 10),
            new Country("DE", "EUR", new BigDecimal("0.80"), 10),
            new Country("CA", "CAD", new BigDecimal("0.80"), 8),
            new Country("AU", "AUD", new BigDecimal("0.80"), 6),
            new Country("JP", "JPY", new BigDecimal("0.65"), 6),
            new Country("SG", "SGD", new BigDecimal("0.75"), 5));

    static final List<Role> ROLES = List.of(
            new Role("Engineering", "Software Engineer", usd(120_000), 40),
            new Role("Engineering", "Senior Software Engineer", usd(160_000), 25),
            new Role("Engineering", "Staff Engineer", usd(200_000), 6),
            new Role("Engineering", "Engineering Manager", usd(190_000), 7),
            new Role("Engineering", "QA Engineer", usd(95_000), 12),
            new Role("Engineering", "DevOps Engineer", usd(130_000), 10),
            new Role("Product", "Product Manager", usd(140_000), 10),
            new Role("Product", "Senior Product Manager", usd(170_000), 4),
            new Role("Product", "Product Designer", usd(115_000), 8),
            new Role("Sales", "Sales Development Representative", usd(60_000), 15),
            new Role("Sales", "Account Executive", usd(90_000), 18),
            new Role("Sales", "Sales Manager", usd(130_000), 5),
            new Role("Marketing", "Marketing Specialist", usd(75_000), 9),
            new Role("Marketing", "Content Strategist", usd(80_000), 5),
            new Role("Marketing", "Marketing Manager", usd(115_000), 4),
            new Role("Finance", "Accountant", usd(70_000), 7),
            new Role("Finance", "Financial Analyst", usd(85_000), 6),
            new Role("Finance", "Finance Manager", usd(130_000), 2),
            new Role("Human Resources", "Recruiter", usd(75_000), 7),
            new Role("Human Resources", "HR Generalist", usd(70_000), 6),
            new Role("Human Resources", "HR Manager", usd(115_000), 2),
            new Role("Customer Support", "Support Specialist", usd(55_000), 20),
            new Role("Customer Support", "Support Team Lead", usd(75_000), 5),
            new Role("Customer Support", "Operations Manager", usd(110_000), 3));

    static final List<String> FIRST_NAMES = List.of(
            "Aarav", "Priya", "Rohan", "Ananya", "Vikram", "Neha", "Arjun", "Kavya",
            "James", "Emma", "Liam", "Olivia", "Noah", "Ava", "Ethan", "Sophia",
            "Oliver", "Amelia", "Harry", "Isla", "Lukas", "Mia", "Felix", "Hannah",
            "Hiroshi", "Yuki", "Kenji", "Sakura", "Wei", "Mei", "Daniel", "Chloe",
            "Lucas", "Grace", "Mateo", "Zoe", "Ryan", "Aisha", "Omar", "Sara");

    static final List<String> LAST_NAMES = List.of(
            "Sharma", "Patel", "Iyer", "Reddy", "Gupta", "Nair", "Singh", "Mehta",
            "Smith", "Johnson", "Williams", "Brown", "Jones", "Miller", "Davis", "Wilson",
            "Taylor", "Clarke", "Evans", "Walker", "Müller", "Schmidt", "Weber", "Fischer",
            "Tanaka", "Sato", "Suzuki", "Watanabe", "Tan", "Lim", "Chen", "Wong",
            "Martin", "Roy", "Thompson", "Lee", "Khan", "Ali", "Garcia", "Lopez");

    private static BigDecimal usd(long amount) {
        return BigDecimal.valueOf(amount);
    }
}

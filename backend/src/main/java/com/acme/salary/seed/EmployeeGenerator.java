package com.acme.salary.seed;

import com.acme.salary.seed.SeedCatalog.Country;
import com.acme.salary.seed.SeedCatalog.Role;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.ToIntFunction;

/**
 * Generates realistic, deterministic employee data.
 * <p>
 * Salary = role base (USD) x country cost factor x individual variation (about ±15%),
 * converted to local currency and rounded to the nearest 100.
 * A small share of employees are deliberately made outliers so the outlier view has data to find.
 * <p>
 * The same random seed always produces exactly the same employees.
 */
public class EmployeeGenerator {

    static final double OUTLIER_RATE = 0.01;
    private static final double VARIATION_STD_DEV = 0.08;
    private static final double MIN_VARIATION = 0.80;
    private static final double MAX_VARIATION = 1.20;
    private static final BigDecimal ROUND_TO = BigDecimal.valueOf(100);
    private static final LocalDate EARLIEST_HIRE = LocalDate.of(2012, 1, 1);
    private static final LocalDate LATEST_HIRE = LocalDate.of(2026, 6, 30);

    private final Map<String, BigDecimal> rateToUsdByCurrency;

    /**
     * @param rateToUsdByCurrency exchange rates (USD per unit of currency) for every seeded currency
     */
    public EmployeeGenerator(Map<String, BigDecimal> rateToUsdByCurrency) {
        this.rateToUsdByCurrency = Map.copyOf(Objects.requireNonNull(rateToUsdByCurrency));
        for (Country country : SeedCatalog.COUNTRIES) {
            if (!this.rateToUsdByCurrency.containsKey(country.currency())) {
                throw new IllegalArgumentException("Missing exchange rate for " + country.currency());
            }
        }
    }

    public List<GeneratedEmployee> generate(int count, long randomSeed) {
        if (count < 0) {
            throw new IllegalArgumentException("count must not be negative");
        }
        Random random = new Random(randomSeed);
        List<GeneratedEmployee> employees = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            employees.add(generateOne(i, random));
        }
        return employees;
    }

    private GeneratedEmployee generateOne(int sequence, Random random) {
        Country country = pickWeighted(SeedCatalog.COUNTRIES, Country::headcountWeight, random);
        Role role = pickWeighted(SeedCatalog.ROLES, Role::weight, random);
        String firstName = pick(SeedCatalog.FIRST_NAMES, random);
        String lastName = pick(SeedCatalog.LAST_NAMES, random);
        String code = "EMP-%05d".formatted(sequence);

        return new GeneratedEmployee(
                code,
                firstName + " " + lastName,
                email(firstName, lastName, sequence),
                role.title(),
                role.department(),
                country.code(),
                country.currency(),
                salary(role, country, random),
                hireDate(random));
    }

    private BigDecimal salary(Role role, Country country, Random random) {
        double variation = clamp(1.0 + random.nextGaussian() * VARIATION_STD_DEV);
        if (random.nextDouble() < OUTLIER_RATE) {
            variation *= random.nextBoolean() ? 1.7 : 0.55;
        }
        BigDecimal usd = role.baseUsd()
                .multiply(country.costFactor())
                .multiply(BigDecimal.valueOf(variation));
        BigDecimal local = usd.divide(rateToUsdByCurrency.get(country.currency()), MathContext.DECIMAL64);
        return roundToNearest(local);
    }

    private static BigDecimal roundToNearest(BigDecimal amount) {
        return amount.divide(ROUND_TO, 0, RoundingMode.HALF_UP).multiply(ROUND_TO).setScale(2);
    }

    private static double clamp(double value) {
        return Math.max(MIN_VARIATION, Math.min(MAX_VARIATION, value));
    }

    private static LocalDate hireDate(Random random) {
        long span = LATEST_HIRE.toEpochDay() - EARLIEST_HIRE.toEpochDay();
        return EARLIEST_HIRE.plusDays((long) (random.nextDouble() * (span + 1)));
    }

    /** The sequence number keeps emails unique even when names repeat. */
    private static String email(String firstName, String lastName, int sequence) {
        return "%s.%s.%05d@acme.com".formatted(asciiLower(firstName), asciiLower(lastName), sequence);
    }

    private static String asciiLower(String value) {
        String withoutAccents = Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return withoutAccents.toLowerCase(Locale.ROOT);
    }

    private static <T> T pick(List<T> items, Random random) {
        return items.get(random.nextInt(items.size()));
    }

    private static <T> T pickWeighted(List<T> items, ToIntFunction<T> weight, Random random) {
        int total = items.stream().mapToInt(weight).sum();
        int roll = random.nextInt(total);
        for (T item : items) {
            roll -= weight.applyAsInt(item);
            if (roll < 0) {
                return item;
            }
        }
        throw new IllegalStateException("unreachable: weights must be positive");
    }
}

package com.acme.salary.employee;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Builds the WHERE clause for the employee directory from an {@link EmployeeFilter}. */
final class EmployeeSpecifications {

    private static final char ESCAPE = '\\';

    private EmployeeSpecifications() {
    }

    static Specification<Employee> matching(EmployeeFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (hasText(filter.search())) {
                String pattern = "%" + escapeLike(filter.search().trim().toLowerCase(Locale.ROOT)) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("fullName")), pattern, ESCAPE),
                        cb.like(cb.lower(root.get("email")), pattern, ESCAPE),
                        cb.like(cb.lower(root.get("employeeCode")), pattern, ESCAPE)));
            }
            if (hasText(filter.countryCode())) {
                predicates.add(cb.equal(root.get("countryCode"), filter.countryCode().trim().toUpperCase(Locale.ROOT)));
            }
            if (hasText(filter.department())) {
                predicates.add(cb.equal(root.get("department"), filter.department().trim()));
            }
            if (hasText(filter.jobTitle())) {
                predicates.add(cb.equal(root.get("jobTitle"), filter.jobTitle().trim()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /** So a search for "100%" means the literal text, not "100 followed by anything". */
    static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

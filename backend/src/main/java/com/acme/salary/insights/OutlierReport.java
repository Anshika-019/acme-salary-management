package com.acme.salary.insights;

import java.math.BigDecimal;
import java.util.List;

/** Outliers sorted by how far they deviate; {@code totalCount} counts all matches, not just the returned page. */
public record OutlierReport(BigDecimal threshold, int minPeerGroupSize, long totalCount, List<Outlier> outliers) {
}

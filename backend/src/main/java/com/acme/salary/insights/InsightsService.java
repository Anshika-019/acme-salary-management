package com.acme.salary.insights;

import com.acme.salary.common.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class InsightsService {

    /** Below this, a "median of peers" is too noisy to call anyone an outlier. */
    static final int MIN_PEER_GROUP_SIZE = 5;
    static final BigDecimal MIN_THRESHOLD = new BigDecimal("0.05");
    static final BigDecimal MAX_THRESHOLD = new BigDecimal("5");
    static final int MAX_LIMIT = 200;

    private final InsightsRepository repository;

    public InsightsService(InsightsRepository repository) {
        this.repository = repository;
    }

    public SalarySummary summary(InsightsFilter filter) {
        return repository.summary(filter);
    }

    public List<GroupStats> statsBy(GroupBy groupBy, InsightsFilter filter) {
        return repository.statsBy(groupBy, filter);
    }

    /**
     * @param threshold minimum relative distance from the peer median, e.g. 0.3 = 30% above or below
     */
    public OutlierReport outliers(InsightsFilter filter, BigDecimal threshold, int limit) {
        if (threshold.compareTo(MIN_THRESHOLD) < 0 || threshold.compareTo(MAX_THRESHOLD) > 0) {
            throw new BadRequestException("threshold must be between " + MIN_THRESHOLD + " and " + MAX_THRESHOLD + ".");
        }
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new BadRequestException("limit must be between 1 and " + MAX_LIMIT + ".");
        }
        return repository.outliers(filter, threshold, MIN_PEER_GROUP_SIZE, limit);
    }
}

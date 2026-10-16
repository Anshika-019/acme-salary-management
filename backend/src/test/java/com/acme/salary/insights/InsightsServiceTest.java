package com.acme.salary.insights;

import com.acme.salary.common.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InsightsServiceTest {

    @Mock
    private InsightsRepository repository;

    @InjectMocks
    private InsightsService service;

    @Test
    void rejectsThresholdOutsideAllowedRange() {
        assertThatThrownBy(() -> service.outliers(InsightsFilter.none(), new BigDecimal("0.01"), 50))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> service.outliers(InsightsFilter.none(), new BigDecimal("10"), 50))
                .isInstanceOf(BadRequestException.class);
        verify(repository, never()).outliers(any(), any(), anyInt(), anyInt());
    }

    @Test
    void rejectsLimitOutsideAllowedRange() {
        assertThatThrownBy(() -> service.outliers(InsightsFilter.none(), new BigDecimal("0.3"), 0))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> service.outliers(InsightsFilter.none(), new BigDecimal("0.3"), 1000))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void usesMinimumPeerGroupSize() {
        BigDecimal threshold = new BigDecimal("0.3");

        service.outliers(InsightsFilter.none(), threshold, 50);

        verify(repository).outliers(any(), eq(threshold), eq(InsightsService.MIN_PEER_GROUP_SIZE), eq(50));
    }
}

package com.acme.salary.insights;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InsightsController.class)
class InsightsControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private InsightsService service;

    @Test
    void salaryStatsDefaultsToGroupingByCountry() throws Exception {
        mvc.perform(get("/api/insights/salary-stats")).andExpect(status().isOk());

        verify(service).statsBy(eq(GroupBy.COUNTRY), any());
    }

    @Test
    void salaryStatsAcceptsJobTitleGrouping() throws Exception {
        mvc.perform(get("/api/insights/salary-stats").param("groupBy", "jobTitle")).andExpect(status().isOk());

        verify(service).statsBy(eq(GroupBy.JOB_TITLE), any());
    }

    @Test
    void salaryStatsRejectsUnknownGrouping() throws Exception {
        mvc.perform(get("/api/insights/salary-stats").param("groupBy", "salary; DROP TABLE employee"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void outliersUseSensibleDefaults() throws Exception {
        mvc.perform(get("/api/insights/outliers")).andExpect(status().isOk());

        verify(service).outliers(any(), eq(new BigDecimal("0.3")), eq(50));
    }
}

package com.acme.salary.insights;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/insights")
public class InsightsController {

    private final InsightsService service;

    public InsightsController(InsightsService service) {
        this.service = service;
    }

    @GetMapping("/summary")
    public SalarySummary summary(@RequestParam(required = false) String country,
                                 @RequestParam(required = false) String department,
                                 @RequestParam(required = false) String jobTitle) {
        return service.summary(new InsightsFilter(country, department, jobTitle));
    }

    @GetMapping("/salary-stats")
    public List<GroupStats> salaryStats(@RequestParam(defaultValue = "country") String groupBy,
                                        @RequestParam(required = false) String country,
                                        @RequestParam(required = false) String department,
                                        @RequestParam(required = false) String jobTitle) {
        return service.statsBy(GroupBy.fromParam(groupBy), new InsightsFilter(country, department, jobTitle));
    }

    @GetMapping("/outliers")
    public OutlierReport outliers(@RequestParam(defaultValue = "0.3") BigDecimal threshold,
                                  @RequestParam(defaultValue = "50") int limit,
                                  @RequestParam(required = false) String country,
                                  @RequestParam(required = false) String department,
                                  @RequestParam(required = false) String jobTitle) {
        return service.outliers(new InsightsFilter(country, department, jobTitle), threshold, limit);
    }
}

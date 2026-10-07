package com.nexushr.analytics;

import com.nexushr.analytics.dto.AttritionRisk;
import com.nexushr.analytics.dto.DashboardDto;
import com.nexushr.common.PeriodUtil;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class AnalyticsController {

    private final AnalyticsService service;

    public AnalyticsController(AnalyticsService service) {
        this.service = service;
    }

    @GetMapping("/dashboard")
    public DashboardDto dashboard(@RequestParam(required = false) String month) {
        return service.dashboard(PeriodUtil.parseMonth(month));
    }

    @GetMapping("/attrition")
    public List<AttritionRisk> attrition() {
        return service.attritionBoard();
    }
}

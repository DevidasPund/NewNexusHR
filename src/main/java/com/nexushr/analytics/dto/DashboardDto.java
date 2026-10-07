package com.nexushr.analytics.dto;

import com.nexushr.performance.dto.ScorePoint;

import java.math.BigDecimal;
import java.util.List;

public record DashboardDto(
        String month,
        long headcount,
        long activeCount,
        long onLeaveCount,
        long presentToday,
        long pendingLeaveApprovals,
        long newHiresThisMonth,
        BigDecimal payrollNetThisMonth,
        int avgPerformanceScore,
        long highRiskCount,
        long mediumRiskCount,
        long lowRiskCount,
        List<HeadcountByDept> headcountByDept,
        List<ScorePoint> scoreTrend) {
}

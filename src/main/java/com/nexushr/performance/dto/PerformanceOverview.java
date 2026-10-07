package com.nexushr.performance.dto;

import java.util.List;

public record PerformanceOverview(
        Long employeeId,
        String employeeName,
        boolean hasReviews,
        int latestScore,
        String grade,
        int delivery,
        int quality,
        int collaboration,
        int ownership,
        int average,
        int best,
        int reviewsCount,
        List<ScorePoint> trend) {
}

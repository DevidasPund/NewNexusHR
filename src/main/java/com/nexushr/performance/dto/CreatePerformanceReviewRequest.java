package com.nexushr.performance.dto;

import com.nexushr.common.enums.ReviewStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreatePerformanceReviewRequest(
        @NotNull Long employeeId,
        @NotBlank String period,
        @Min(0) @Max(100) int delivery,
        @Min(0) @Max(100) int quality,
        @Min(0) @Max(100) int collaboration,
        @Min(0) @Max(100) int ownership,
        Integer overallScore,
        String comments,
        ReviewStatus status) {
}

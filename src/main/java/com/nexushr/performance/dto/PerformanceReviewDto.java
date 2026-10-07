package com.nexushr.performance.dto;

import com.nexushr.common.enums.ReviewStatus;
import com.nexushr.performance.PerformanceReview;

import java.time.Instant;
import java.time.LocalDate;

public record PerformanceReviewDto(
        Long id,
        Long employeeId,
        String employeeName,
        String empCode,
        String avatarColor,
        String designation,
        String department,
        String period,
        int overallScore,
        String grade,
        int delivery,
        int quality,
        int collaboration,
        int ownership,
        Long reviewerId,
        String reviewerName,
        String comments,
        ReviewStatus status,
        LocalDate reviewDate,
        Instant createdAt) {

    public static PerformanceReviewDto from(PerformanceReview r) {
        var e = r.getEmployee();
        return new PerformanceReviewDto(
                r.getId(),
                e.getId(),
                e.getFullName(),
                e.getEmpCode(),
                e.getAvatarColor(),
                e.getDesignation(),
                e.getDepartment() != null ? e.getDepartment().getName() : null,
                r.getPeriod(),
                r.getOverallScore(),
                r.getGrade(),
                r.getDelivery(),
                r.getQuality(),
                r.getCollaboration(),
                r.getOwnership(),
                r.getReviewerId(),
                r.getReviewerName(),
                r.getComments(),
                r.getStatus(),
                r.getReviewDate(),
                r.getCreatedAt());
    }
}

package com.nexushr.analytics.dto;

import com.nexushr.common.enums.RiskBand;

import java.util.List;

public record AttritionRisk(
        Long employeeId,
        String employeeName,
        String empCode,
        String avatarColor,
        String department,
        String designation,
        int score,
        RiskBand band,
        List<String> factors,
        int tenureMonths,
        int attendanceRate,
        int lastScore) {
}

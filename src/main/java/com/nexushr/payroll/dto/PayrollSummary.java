package com.nexushr.payroll.dto;

import java.math.BigDecimal;

public record PayrollSummary(
        String periodMonth,
        long employees,
        BigDecimal totalGross,
        BigDecimal totalDeductions,
        BigDecimal totalNet,
        long paid,
        long pending) {
}

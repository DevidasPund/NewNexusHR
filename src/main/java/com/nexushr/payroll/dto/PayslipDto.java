package com.nexushr.payroll.dto;

import com.nexushr.common.enums.PayslipStatus;
import com.nexushr.payroll.Payslip;

import java.math.BigDecimal;
import java.time.Instant;

public record PayslipDto(
        Long id,
        Long employeeId,
        String employeeName,
        String empCode,
        String avatarColor,
        String designation,
        String department,
        String periodMonth,
        BigDecimal basic,
        BigDecimal hra,
        BigDecimal allowances,
        BigDecimal gross,
        BigDecimal pf,
        BigDecimal tax,
        BigDecimal otherDeductions,
        BigDecimal totalDeductions,
        BigDecimal netPay,
        PayslipStatus status,
        Instant generatedAt,
        Instant paidAt) {

    public static PayslipDto from(Payslip p) {
        var e = p.getEmployee();
        return new PayslipDto(
                p.getId(),
                e.getId(),
                e.getFullName(),
                e.getEmpCode(),
                e.getAvatarColor(),
                e.getDesignation(),
                e.getDepartment() != null ? e.getDepartment().getName() : null,
                p.getPeriodMonth(),
                p.getBasic(),
                p.getHra(),
                p.getAllowances(),
                p.getGross(),
                p.getPf(),
                p.getTax(),
                p.getOtherDeductions(),
                p.getTotalDeductions(),
                p.getNetPay(),
                p.getStatus(),
                p.getCreatedAt(),
                p.getPaidAt());
    }
}

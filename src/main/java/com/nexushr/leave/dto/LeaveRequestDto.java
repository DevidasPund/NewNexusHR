package com.nexushr.leave.dto;

import com.nexushr.common.enums.LeaveStatus;
import com.nexushr.common.enums.LeaveType;
import com.nexushr.leave.LeaveRequest;

import java.time.Instant;
import java.time.LocalDate;

public record LeaveRequestDto(
        Long id,
        Long employeeId,
        String employeeName,
        String empCode,
        String avatarColor,
        LeaveType type,
        LocalDate startDate,
        LocalDate endDate,
        int days,
        String reason,
        LeaveStatus status,
        Long approverId,
        String approverName,
        Instant appliedAt,
        Instant decidedAt) {

    public static LeaveRequestDto from(LeaveRequest r) {
        var e = r.getEmployee();
        return new LeaveRequestDto(
                r.getId(),
                e.getId(),
                e.getFullName(),
                e.getEmpCode(),
                e.getAvatarColor(),
                r.getType(),
                r.getStartDate(),
                r.getEndDate(),
                r.getDays(),
                r.getReason(),
                r.getStatus(),
                r.getApproverId(),
                r.getApproverName(),
                r.getCreatedAt(),
                r.getDecidedAt());
    }
}

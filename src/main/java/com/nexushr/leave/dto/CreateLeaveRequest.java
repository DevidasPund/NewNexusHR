package com.nexushr.leave.dto;

import com.nexushr.common.enums.LeaveType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateLeaveRequest(
        @NotNull LeaveType type,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        String reason) {
}

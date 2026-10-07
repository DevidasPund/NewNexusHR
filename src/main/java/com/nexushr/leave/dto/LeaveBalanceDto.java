package com.nexushr.leave.dto;

import com.nexushr.common.enums.LeaveType;
import com.nexushr.leave.LeaveBalance;

public record LeaveBalanceDto(
        LeaveType type,
        double allocated,
        double used,
        double remaining) {

    public static LeaveBalanceDto from(LeaveBalance b) {
        return new LeaveBalanceDto(
                b.getType(),
                b.getAllocated(),
                b.getUsed(),
                Math.max(0, b.getAllocated() - b.getUsed()));
    }
}

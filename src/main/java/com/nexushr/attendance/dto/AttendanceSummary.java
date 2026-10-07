package com.nexushr.attendance.dto;

/** Monthly attendance rollup for one employee. */
public record AttendanceSummary(
        int present,
        int late,
        int wfh,
        int halfDay,
        int absent,
        int onLeave,
        int workingDays,
        double attendanceRate,
        double avgHours
) {
}

package com.nexushr.attendance.dto;

import com.nexushr.attendance.Attendance;
import com.nexushr.common.enums.AttendanceStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public record AttendanceDto(
        Long id,
        Long employeeId,
        String employeeName,
        String empCode,
        String avatarColor,
        LocalDate date,
        LocalTime clockIn,
        LocalTime clockOut,
        AttendanceStatus status,
        double workedHours
) {
    public static AttendanceDto from(Attendance a) {
        return new AttendanceDto(
                a.getId(),
                a.getEmployee().getId(),
                a.getEmployee().getFullName(),
                a.getEmployee().getEmpCode(),
                a.getEmployee().getAvatarColor(),
                a.getDate(),
                a.getClockIn(),
                a.getClockOut(),
                a.getStatus(),
                a.getWorkedHours()
        );
    }
}

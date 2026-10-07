package com.nexushr.attendance;

import com.nexushr.attendance.dto.AttendanceDto;
import com.nexushr.attendance.dto.AttendanceSummary;
import com.nexushr.common.PeriodUtil;
import com.nexushr.security.UserPrincipal;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService service;

    public AttendanceController(AttendanceService service) {
        this.service = service;
    }

    // =========================
    // LOGGED-IN EMPLOYEE
    // =========================

    @GetMapping("/me")
    public List<AttendanceDto> myMonth(
            @AuthenticationPrincipal UserPrincipal me,
            @RequestParam(required = false) String month) {

        return service.monthly(
                me.getId(),
                PeriodUtil.parseMonth(month)
        );
    }

    @GetMapping("/today/me")
    public AttendanceDto today(
            @AuthenticationPrincipal UserPrincipal me) {

        return service.getToday(me.getId());
    }

    @GetMapping("/summary/me")
    public AttendanceSummary mySummary(
            @AuthenticationPrincipal UserPrincipal me,
            @RequestParam(required = false) String month) {

        return service.summary(
                me.getId(),
                PeriodUtil.parseMonth(month)
        );
    }

    // Employee clicks Clock In
    @PostMapping("/clock-in")
    public AttendanceDto clockIn(
            @AuthenticationPrincipal UserPrincipal me) {

        return service.clockIn(me.getId());
    }

    // Employee clicks Clock Out
    @PostMapping("/clock-out")
    public AttendanceDto clockOut(
            @AuthenticationPrincipal UserPrincipal me) {

        return service.clockOut(me.getId());
    }

    // =========================
    // MANAGER / ADMIN
    // =========================

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public List<AttendanceDto> forEmployee(
            @RequestParam Long employeeId,
            @RequestParam(required = false) String month) {

        return service.monthly(
                employeeId,
                PeriodUtil.parseMonth(month)
        );
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public AttendanceSummary summaryForEmployee(
            @RequestParam Long employeeId,
            @RequestParam(required = false) String month) {

        return service.summary(
                employeeId,
                PeriodUtil.parseMonth(month)
        );
    }

    // Manager/Admin sees all employees for selected date
    @GetMapping("/by-date")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public List<AttendanceDto> byDate(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {

        return service.byDate(
                date == null ? LocalDate.now() : date
        );
    }
}
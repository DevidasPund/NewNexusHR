package com.nexushr.attendance;

import com.nexushr.common.BaseEntity;
import com.nexushr.common.enums.AttendanceStatus;
import com.nexushr.employee.Employee;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "attendance", uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "work_date"}))
public class Attendance extends BaseEntity {

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(name = "work_date", nullable = false)
    private LocalDate date;

    private LocalTime clockIn;

    private LocalTime clockOut;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status = AttendanceStatus.PRESENT;

    private double workedHours;

    public Attendance() {
    }

    public Attendance(Employee employee, LocalDate date, AttendanceStatus status) {
        this.employee = employee;
        this.date = date;
        this.status = status;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getClockIn() {
        return clockIn;
    }

    public void setClockIn(LocalTime clockIn) {
        this.clockIn = clockIn;
    }

    public LocalTime getClockOut() {
        return clockOut;
    }

    public void setClockOut(LocalTime clockOut) {
        this.clockOut = clockOut;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }

    public double getWorkedHours() {
        return workedHours;
    }

    public void setWorkedHours(double workedHours) {
        this.workedHours = workedHours;
    }
}

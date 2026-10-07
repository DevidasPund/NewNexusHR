package com.nexushr.leave;

import com.nexushr.common.BaseEntity;
import com.nexushr.common.enums.LeaveType;
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

@Entity
@Table(name = "leave_balances", uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "type"}))
public class LeaveBalance extends BaseEntity {

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveType type;

    private double allocated;

    private double used;

    public LeaveBalance() {
    }

    public LeaveBalance(Employee employee, LeaveType type, double allocated) {
        this.employee = employee;
        this.type = type;
        this.allocated = allocated;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public LeaveType getType() {
        return type;
    }

    public void setType(LeaveType type) {
        this.type = type;
    }

    public double getAllocated() {
        return allocated;
    }

    public void setAllocated(double allocated) {
        this.allocated = allocated;
    }

    public double getUsed() {
        return used;
    }

    public void setUsed(double used) {
        this.used = used;
    }
}

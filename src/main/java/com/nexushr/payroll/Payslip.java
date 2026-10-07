package com.nexushr.payroll;

import com.nexushr.common.BaseEntity;
import com.nexushr.common.enums.PayslipStatus;
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

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payslips", uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "period_month"}))
public class Payslip extends BaseEntity {

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(name = "period_month", nullable = false, length = 7)
    private String periodMonth; // yyyy-MM

    @Column(precision = 12, scale = 2)
    private BigDecimal basic = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal hra = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal allowances = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal gross = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal pf = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal tax = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal otherDeductions = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal totalDeductions = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal netPay = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayslipStatus status = PayslipStatus.DRAFT;

    private Instant paidAt;

    public Payslip() {
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public String getPeriodMonth() {
        return periodMonth;
    }

    public void setPeriodMonth(String periodMonth) {
        this.periodMonth = periodMonth;
    }

    public BigDecimal getBasic() {
        return basic;
    }

    public void setBasic(BigDecimal basic) {
        this.basic = basic;
    }

    public BigDecimal getHra() {
        return hra;
    }

    public void setHra(BigDecimal hra) {
        this.hra = hra;
    }

    public BigDecimal getAllowances() {
        return allowances;
    }

    public void setAllowances(BigDecimal allowances) {
        this.allowances = allowances;
    }

    public BigDecimal getGross() {
        return gross;
    }

    public void setGross(BigDecimal gross) {
        this.gross = gross;
    }

    public BigDecimal getPf() {
        return pf;
    }

    public void setPf(BigDecimal pf) {
        this.pf = pf;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public BigDecimal getOtherDeductions() {
        return otherDeductions;
    }

    public void setOtherDeductions(BigDecimal otherDeductions) {
        this.otherDeductions = otherDeductions;
    }

    public BigDecimal getTotalDeductions() {
        return totalDeductions;
    }

    public void setTotalDeductions(BigDecimal totalDeductions) {
        this.totalDeductions = totalDeductions;
    }

    public BigDecimal getNetPay() {
        return netPay;
    }

    public void setNetPay(BigDecimal netPay) {
        this.netPay = netPay;
    }

    public PayslipStatus getStatus() {
        return status;
    }

    public void setStatus(PayslipStatus status) {
        this.status = status;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(Instant paidAt) {
        this.paidAt = paidAt;
    }
}

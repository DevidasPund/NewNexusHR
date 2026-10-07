package com.nexushr.payroll;

import com.nexushr.common.enums.EmployeeStatus;
import com.nexushr.common.enums.PayslipStatus;
import com.nexushr.common.exception.ResourceNotFoundException;
import com.nexushr.employee.Employee;
import com.nexushr.employee.EmployeeRepository;
import com.nexushr.payroll.dto.PayrollSummary;
import com.nexushr.payroll.dto.PayslipDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.YearMonth;
import java.util.List;

@Service
public class PayrollService {

    private static final BigDecimal BASIC_PCT = new BigDecimal("0.50");
    private static final BigDecimal HRA_PCT = new BigDecimal("0.20");
    private static final BigDecimal PF_PCT = new BigDecimal("0.12");
    private static final BigDecimal TAX_PCT = new BigDecimal("0.10");
    private static final BigDecimal PROFESSIONAL_TAX = new BigDecimal("200.00");
    private static final BigDecimal MONTHS = new BigDecimal("12");

    private final PayslipRepository payslipRepo;
    private final EmployeeRepository employeeRepo;

    public PayrollService(PayslipRepository payslipRepo, EmployeeRepository employeeRepo) {
        this.payslipRepo = payslipRepo;
        this.employeeRepo = employeeRepo;
    }

    @Transactional(readOnly = true)
    public List<PayslipDto> listForEmployee(Long employeeId) {
        return payslipRepo.findByEmployee_IdOrderByPeriodMonthDesc(employeeId).stream()
                .map(PayslipDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PayslipDto> listForMonth(YearMonth month) {
        return payslipRepo.findByPeriodMonthOrderByEmployee_FirstNameAsc(month.toString()).stream()
                .map(PayslipDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PayslipDto get(Long id) {
        return PayslipDto.from(load(id));
    }

    /** Generates (or regenerates) payslips for every active employee for the given month. */
    @Transactional
    public List<PayslipDto> generateForMonth(YearMonth month) {
        List<Employee> active = employeeRepo.findByStatus(EmployeeStatus.ACTIVE);
        for (Employee e : active) {
            upsert(e, month.toString());
        }
        return listForMonth(month);
    }

    @Transactional
    public PayslipDto generateForEmployee(Long employeeId, YearMonth month) {
        Employee e = employeeRepo.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", employeeId));
        return PayslipDto.from(upsert(e, month.toString()));
    }

    @Transactional
    public PayslipDto markPaid(Long id) {
        Payslip p = load(id);
        p.setStatus(PayslipStatus.PAID);
        p.setPaidAt(Instant.now());
        return PayslipDto.from(payslipRepo.save(p));
    }

    @Transactional(readOnly = true)
    public PayrollSummary summary(YearMonth month) {
        List<Payslip> slips = payslipRepo.findByPeriodMonthOrderByEmployee_FirstNameAsc(month.toString());
        BigDecimal gross = BigDecimal.ZERO;
        BigDecimal deductions = BigDecimal.ZERO;
        BigDecimal net = BigDecimal.ZERO;
        long paid = 0;
        for (Payslip p : slips) {
            gross = gross.add(nz(p.getGross()));
            deductions = deductions.add(nz(p.getTotalDeductions()));
            net = net.add(nz(p.getNetPay()));
            if (p.getStatus() == PayslipStatus.PAID) {
                paid++;
            }
        }
        return new PayrollSummary(month.toString(), slips.size(), gross, deductions, net,
                paid, slips.size() - paid);
    }

    private Payslip upsert(Employee e, String period) {
        Payslip p = payslipRepo.findByEmployee_IdAndPeriodMonth(e.getId(), period)
                .orElseGet(Payslip::new);
        // Do not overwrite an already-paid slip.
        if (p.getId() != null && p.getStatus() == PayslipStatus.PAID) {
            return p;
        }
        p.setEmployee(e);
        p.setPeriodMonth(period);
        compute(p, nz(e.getBaseSalary()));
        p.setStatus(PayslipStatus.GENERATED);
        return payslipRepo.save(p);
    }

    /** Derives a payslip breakdown from an annual CTC. */
    private void compute(Payslip p, BigDecimal annualCtc) {
        BigDecimal monthlyGross = scale(annualCtc.divide(MONTHS, 2, RoundingMode.HALF_UP));
        BigDecimal basic = scale(monthlyGross.multiply(BASIC_PCT));
        BigDecimal hra = scale(monthlyGross.multiply(HRA_PCT));
        BigDecimal allowances = scale(monthlyGross.subtract(basic).subtract(hra));
        BigDecimal pf = scale(basic.multiply(PF_PCT));
        BigDecimal tax = scale(monthlyGross.multiply(TAX_PCT));
        BigDecimal totalDeductions = scale(pf.add(tax).add(PROFESSIONAL_TAX));
        BigDecimal net = scale(monthlyGross.subtract(totalDeductions));

        p.setBasic(basic);
        p.setHra(hra);
        p.setAllowances(allowances);
        p.setGross(monthlyGross);
        p.setPf(pf);
        p.setTax(tax);
        p.setOtherDeductions(PROFESSIONAL_TAX);
        p.setTotalDeductions(totalDeductions);
        p.setNetPay(net);
    }

    private Payslip load(Long id) {
        return payslipRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payslip", id));
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private static BigDecimal scale(BigDecimal v) {
        return v.setScale(2, RoundingMode.HALF_UP);
    }
}

package com.nexushr.payroll;

import com.nexushr.common.PeriodUtil;
import com.nexushr.payroll.dto.PayrollSummary;
import com.nexushr.payroll.dto.PayslipDto;
import com.nexushr.security.UserPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/payroll")
public class PayrollController {

    private final PayrollService service;

    public PayrollController(PayrollService service) {
        this.service = service;
    }

    @GetMapping("/me")
    public List<PayslipDto> myPayslips(@AuthenticationPrincipal UserPrincipal me) {
        return service.listForEmployee(me.getId());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public List<PayslipDto> forMonth(@RequestParam(required = false) String month) {
        return service.listForMonth(PeriodUtil.parseMonth(month));
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public PayrollSummary summary(@RequestParam(required = false) String month) {
        return service.summary(PeriodUtil.parseMonth(month));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public List<PayslipDto> forEmployee(@PathVariable Long employeeId) {
        return service.listForEmployee(employeeId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public PayslipDto get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping("/generate")
    @PreAuthorize("hasRole('ADMIN')")
    public List<PayslipDto> generate(@RequestParam(required = false) String month) {
        return service.generateForMonth(PeriodUtil.parseMonth(month));
    }

    @PostMapping("/generate/{employeeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public PayslipDto generateForEmployee(@PathVariable Long employeeId,
                                          @RequestParam(required = false) String month) {
        return service.generateForEmployee(employeeId, PeriodUtil.parseMonth(month));
    }

    @PostMapping("/{id}/pay")
    @PreAuthorize("hasRole('ADMIN')")
    public PayslipDto markPaid(@PathVariable Long id) {
        return service.markPaid(id);
    }
}

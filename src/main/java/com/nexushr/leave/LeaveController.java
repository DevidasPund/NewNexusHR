package com.nexushr.leave;

import com.nexushr.leave.dto.CreateLeaveRequest;
import com.nexushr.leave.dto.LeaveBalanceDto;
import com.nexushr.leave.dto.LeaveRequestDto;
import com.nexushr.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/leaves")
public class LeaveController {

    private final LeaveService service;

    public LeaveController(LeaveService service) {
        this.service = service;
    }

    // ----- self-service -----

    @GetMapping("/me")
    public List<LeaveRequestDto> myRequests(@AuthenticationPrincipal UserPrincipal me) {
        return service.listForEmployee(me.getId());
    }

    @GetMapping("/balances/me")
    public List<LeaveBalanceDto> myBalances(@AuthenticationPrincipal UserPrincipal me) {
        return service.balancesForEmployee(me.getId());
    }

    @PostMapping
    public LeaveRequestDto apply(@AuthenticationPrincipal UserPrincipal me,
                                 @Valid @RequestBody CreateLeaveRequest req) {
        return service.create(me.getId(), req);
    }

    @PostMapping("/{id}/cancel")
    public LeaveRequestDto cancel(@AuthenticationPrincipal UserPrincipal me, @PathVariable Long id) {
        return service.cancel(id, me.getId());
    }

    // ----- management -----

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public List<LeaveRequestDto> all() {
        return service.listAll();
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public List<LeaveRequestDto> pending() {
        return service.listPending();
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public List<LeaveRequestDto> forEmployee(@PathVariable Long employeeId) {
        return service.listForEmployee(employeeId);
    }

    @GetMapping("/balances/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public List<LeaveBalanceDto> balances(@PathVariable Long employeeId) {
        return service.balancesForEmployee(employeeId);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public LeaveRequestDto approve(@AuthenticationPrincipal UserPrincipal me, @PathVariable Long id) {
        return service.approve(id, me.getId());
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public LeaveRequestDto reject(@AuthenticationPrincipal UserPrincipal me, @PathVariable Long id) {
        return service.reject(id, me.getId());
    }
}

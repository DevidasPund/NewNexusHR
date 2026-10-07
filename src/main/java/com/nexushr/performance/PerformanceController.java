package com.nexushr.performance;

import com.nexushr.performance.dto.CreatePerformanceReviewRequest;
import com.nexushr.performance.dto.PerformanceOverview;
import com.nexushr.performance.dto.PerformanceReviewDto;
import com.nexushr.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/performance")
public class PerformanceController {

    private final PerformanceService service;

    public PerformanceController(PerformanceService service) {
        this.service = service;
    }

    @GetMapping("/me")
    public List<PerformanceReviewDto> myReviews(@AuthenticationPrincipal UserPrincipal me) {
        return service.listForEmployee(me.getId());
    }

    @GetMapping("/overview/me")
    public PerformanceOverview myOverview(@AuthenticationPrincipal UserPrincipal me) {
        return service.overview(me.getId());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public List<PerformanceReviewDto> all() {
        return service.listAll();
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public List<PerformanceReviewDto> forEmployee(@PathVariable Long employeeId) {
        return service.listForEmployee(employeeId);
    }

    @GetMapping("/overview/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public PerformanceOverview overview(@PathVariable Long employeeId) {
        return service.overview(employeeId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public PerformanceReviewDto get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public PerformanceReviewDto create(@AuthenticationPrincipal UserPrincipal me,
                                       @Valid @RequestBody CreatePerformanceReviewRequest req) {
        return service.create(req, me.getId());
    }
}

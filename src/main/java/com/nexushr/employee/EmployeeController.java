package com.nexushr.employee;

import com.nexushr.common.enums.EmployeeStatus;
import com.nexushr.common.enums.Role;
import com.nexushr.employee.dto.CreateEmployeeRequest;
import com.nexushr.employee.dto.EmployeeDto;
import com.nexushr.employee.dto.UpdateEmployeeRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    // ADMIN and MANAGER can see all employees
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public List<EmployeeDto> list(
            @RequestParam(required = false) EmployeeStatus status,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String q) {

        return service.list(status, departmentId, q);
    }

    // ADMIN and MANAGER can open any employee
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public EmployeeDto get(@PathVariable Long id) {
        return service.get(id);
    }

    // Only ADMIN can create
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeDto create(
            @Valid @RequestBody CreateEmployeeRequest req) {

        return service.create(req);
    }

    // Only ADMIN can update
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public EmployeeDto update(
            @PathVariable Long id,
            @RequestBody UpdateEmployeeRequest req) {

        return service.update(id, req);
    }

    // Only ADMIN can deactivate
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable Long id) {

        service.deactivate(id);
    }
}
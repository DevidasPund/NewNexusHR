package com.nexushr.department;

import com.nexushr.department.dto.DepartmentDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService service;

    public DepartmentController(DepartmentService service) {
        this.service = service;
    }

    @GetMapping
    public List<DepartmentDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public DepartmentDto get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public DepartmentDto create(@RequestBody CreateDepartmentRequest req) {
        return service.create(req.name(), req.code(), req.colorKey());
    }

    public record CreateDepartmentRequest(String name, String code, String colorKey) {
    }
}

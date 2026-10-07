package com.nexushr.department;

import com.nexushr.common.exception.ResourceNotFoundException;
import com.nexushr.department.dto.DepartmentDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentService {

    private final DepartmentRepository repository;

    public DepartmentService(DepartmentRepository repository) {
        this.repository = repository;
    }

    public List<DepartmentDto> listAll() {
        return repository.findAll().stream()
                .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                .map(DepartmentDto::from)
                .toList();
    }

    public DepartmentDto get(Long id) {
        return DepartmentDto.from(getEntity(id));
    }

    public Department getEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", id));
    }

    public DepartmentDto create(String name, String code, String colorKey) {
        Department d = new Department(name, code, colorKey == null ? "violet" : colorKey);
        return DepartmentDto.from(repository.save(d));
    }
}

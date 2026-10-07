package com.nexushr.department.dto;

import com.nexushr.department.Department;

public record DepartmentDto(Long id, String name, String code, String colorKey, Long headId) {
    public static DepartmentDto from(Department d) {
        if (d == null) {
            return null;
        }
        return new DepartmentDto(d.getId(), d.getName(), d.getCode(), d.getColorKey(), d.getHeadId());
    }
}

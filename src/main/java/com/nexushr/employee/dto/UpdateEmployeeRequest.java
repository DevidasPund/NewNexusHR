package com.nexushr.employee.dto;

import com.nexushr.common.enums.EmployeeStatus;
import com.nexushr.common.enums.Role;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** All fields optional — only non-null values are applied. */
public record UpdateEmployeeRequest(
        String firstName,
        String lastName,
        String phone,
        Role role,
        EmployeeStatus status,
        Long departmentId,
        String designation,
        Long managerId,
        LocalDate dateOfJoining,
        LocalDate lastPromotionDate,
        BigDecimal baseSalary,
        String location,
        String employmentType,
        String gender,
        String avatarColor,
        String bio,
        List<String> skills
) {
}

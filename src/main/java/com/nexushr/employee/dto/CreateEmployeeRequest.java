package com.nexushr.employee.dto;

import com.nexushr.common.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CreateEmployeeRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank @Email String email,
        String phone,
        Role role,
        Long departmentId,
        String designation,
        Long managerId,
        LocalDate dateOfJoining,
        BigDecimal baseSalary,
        String location,
        String employmentType,
        String gender,
        String avatarColor,
        String bio,
        List<String> skills,
        String password
) {
}

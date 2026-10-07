package com.nexushr.employee.dto;

import com.nexushr.common.enums.EmployeeStatus;
import com.nexushr.common.enums.Role;
import com.nexushr.department.dto.DepartmentDto;
import com.nexushr.employee.Employee;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record EmployeeDto(
        Long id,
        String empCode,
        String firstName,
        String lastName,
        String fullName,
        String initials,
        String email,
        String phone,
        Role role,
        EmployeeStatus status,
        DepartmentDto department,
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
    public static EmployeeDto from(Employee e) {
        if (e == null) {
            return null;
        }
        return new EmployeeDto(
                e.getId(),
                e.getEmpCode(),
                e.getFirstName(),
                e.getLastName(),
                e.getFullName(),
                e.getInitials(),
                e.getEmail(),
                e.getPhone(),
                e.getRole(),
                e.getStatus(),
                DepartmentDto.from(e.getDepartment()),
                e.getDesignation(),
                e.getManagerId(),
                e.getDateOfJoining(),
                e.getLastPromotionDate(),
                e.getBaseSalary(),
                e.getLocation(),
                e.getEmploymentType(),
                e.getGender(),
                e.getAvatarColor(),
                e.getBio(),
                List.copyOf(e.getSkills())
        );
    }
}

package com.nexushr.auth.dto;

import com.nexushr.employee.dto.EmployeeDto;

public record AuthResponse(
        String token,
        String tokenType,
        long expiresInMs,
        EmployeeDto user
) {
}

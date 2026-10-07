package com.nexushr.auth;

import com.nexushr.auth.dto.AuthResponse;
import com.nexushr.auth.dto.LoginRequest;
import com.nexushr.common.exception.ResourceNotFoundException;
import com.nexushr.employee.Employee;
import com.nexushr.employee.EmployeeRepository;
import com.nexushr.employee.dto.EmployeeDto;
import com.nexushr.security.JwtService;
import com.nexushr.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final EmployeeRepository employeeRepository;
    private final JwtService jwtService;

    public AuthController(
            AuthenticationManager authenticationManager,
            EmployeeRepository employeeRepository,
            JwtService jwtService) {

        this.authenticationManager =
                authenticationManager;

        this.employeeRepository =
                employeeRepository;

        this.jwtService =
                jwtService;
    }

    @PostMapping("/login")
    public AuthResponse login(
            @Valid @RequestBody LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        Employee employee =
                employeeRepository
                        .findByEmail(request.email())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Account",
                                        request.email()
                                )
                        );

        String token =
                jwtService.generateToken(
                        UserPrincipal.from(employee)
                );

        return new AuthResponse(
                token,
                "Bearer",
                jwtService.getExpirationMs(),
                EmployeeDto.from(employee)
        );
    }

    // Current logged-in employee
    @GetMapping("/me")
    public EmployeeDto me(
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            throw new ResourceNotFoundException(
                    "Authenticated employee",
                    "current"
            );
        }

        Employee employee =
                employeeRepository
                        .findById(principal.getId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Employee",
                                        principal.getId()
                                )
                        );

        return EmployeeDto.from(employee);
    }
}
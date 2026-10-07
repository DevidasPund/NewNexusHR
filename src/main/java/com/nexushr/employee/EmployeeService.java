package com.nexushr.employee;

import com.nexushr.common.enums.EmployeeStatus;
import com.nexushr.common.enums.Role;
import com.nexushr.common.exception.BadRequestException;
import com.nexushr.common.exception.ResourceNotFoundException;
import com.nexushr.department.Department;
import com.nexushr.department.DepartmentRepository;
import com.nexushr.employee.dto.CreateEmployeeRequest;
import com.nexushr.employee.dto.EmployeeDto;
import com.nexushr.employee.dto.UpdateEmployeeRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class EmployeeService {

    private final EmployeeRepository repository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final String seedPassword;

    public EmployeeService(
            EmployeeRepository repository,
            DepartmentRepository departmentRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.seed.password}") String seedPassword) {

        this.repository = repository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.seedPassword = seedPassword;
    }

    public List<EmployeeDto> list(
            EmployeeStatus status,
            Long departmentId,
            String q) {

        String needle =
                q == null ? null : q.trim().toLowerCase(Locale.ROOT);

        return repository.findAll()
                .stream()

                .filter(e ->
                        status == null ||
                        e.getStatus() == status)

                .filter(e ->
                        departmentId == null ||
                        (
                            e.getDepartment() != null &&
                            departmentId.equals(
                                    e.getDepartment().getId())
                        )
                )

                .filter(e ->
                        needle == null ||
                        needle.isBlank() ||
                        matches(e, needle)
                )

                .sorted(
                        Comparator.comparing(
                                Employee::getFullName,
                                String.CASE_INSENSITIVE_ORDER
                        )
                )

                .map(EmployeeDto::from)

                .toList();
    }

    private boolean matches(
            Employee e,
            String needle) {

        return e.getFullName()
                .toLowerCase(Locale.ROOT)
                .contains(needle)

                || (
                    e.getEmail() != null &&
                    e.getEmail()
                            .toLowerCase(Locale.ROOT)
                            .contains(needle)
                )

                || (
                    e.getEmpCode() != null &&
                    e.getEmpCode()
                            .toLowerCase(Locale.ROOT)
                            .contains(needle)
                )

                || (
                    e.getDesignation() != null &&
                    e.getDesignation()
                            .toLowerCase(Locale.ROOT)
                            .contains(needle)
                );
    }

    public EmployeeDto get(Long id) {

        return EmployeeDto.from(
                getEntity(id)
        );
    }

    public Employee getEntity(Long id) {

        return repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee",
                                id
                        )
                );
    }

    public EmployeeDto create(
            CreateEmployeeRequest req) {

        if (repository.existsByEmail(req.email())) {
            throw new BadRequestException(
                    "An employee with that email already exists"
            );
        }

        Employee e = new Employee();

        e.setFirstName(req.firstName());
        e.setLastName(req.lastName());
        e.setEmail(req.email());
        e.setPhone(req.phone());

        e.setRole(
                req.role() == null
                        ? Role.EMPLOYEE
                        : req.role()
        );

        e.setStatus(EmployeeStatus.ACTIVE);

        e.setDesignation(req.designation());

        e.setManagerId(req.managerId());

        e.setDateOfJoining(
                req.dateOfJoining() == null
                        ? LocalDate.now()
                        : req.dateOfJoining()
        );

        e.setBaseSalary(
                req.baseSalary() == null
                        ? BigDecimal.ZERO
                        : req.baseSalary()
        );

        e.setLocation(req.location());

        e.setEmploymentType(
                req.employmentType() == null
                        ? "Full-time"
                        : req.employmentType()
        );

        e.setGender(req.gender());

        e.setAvatarColor(
                req.avatarColor() == null
                        ? "violet"
                        : req.avatarColor()
        );

        e.setBio(req.bio());

        if (req.skills() != null) {
            e.setSkills(req.skills());
        }

        if (req.departmentId() != null) {
            e.setDepartment(
                    loadDepartment(req.departmentId())
            );
        }

        String raw =
                req.password() == null ||
                req.password().isBlank()
                        ? seedPassword
                        : req.password();

        e.setPasswordHash(
                passwordEncoder.encode(raw)
        );

        e.setEmpCode(nextEmpCode());

        return EmployeeDto.from(
                repository.save(e)
        );
    }

    public EmployeeDto update(
            Long id,
            UpdateEmployeeRequest req) {

        Employee e = getEntity(id);

        if (req.firstName() != null)
            e.setFirstName(req.firstName());

        if (req.lastName() != null)
            e.setLastName(req.lastName());

        if (req.phone() != null)
            e.setPhone(req.phone());

        if (req.role() != null)
            e.setRole(req.role());

        if (req.status() != null)
            e.setStatus(req.status());

        if (req.designation() != null)
            e.setDesignation(req.designation());

        if (req.managerId() != null)
            e.setManagerId(req.managerId());

        if (req.dateOfJoining() != null)
            e.setDateOfJoining(req.dateOfJoining());

        if (req.lastPromotionDate() != null)
            e.setLastPromotionDate(
                    req.lastPromotionDate()
            );

        if (req.baseSalary() != null)
            e.setBaseSalary(req.baseSalary());

        if (req.location() != null)
            e.setLocation(req.location());

        if (req.employmentType() != null)
            e.setEmploymentType(
                    req.employmentType()
            );

        if (req.gender() != null)
            e.setGender(req.gender());

        if (req.avatarColor() != null)
            e.setAvatarColor(
                    req.avatarColor()
            );

        if (req.bio() != null)
            e.setBio(req.bio());

        if (req.skills() != null)
            e.setSkills(req.skills());

        if (req.departmentId() != null)
            e.setDepartment(
                    loadDepartment(req.departmentId())
            );

        return EmployeeDto.from(
                repository.save(e)
        );
    }

    public void deactivate(Long id) {

        Employee e = getEntity(id);

        e.setStatus(
                EmployeeStatus.INACTIVE
        );

        repository.save(e);
    }

    private Department loadDepartment(Long id) {

        return departmentRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Department",
                                id
                        )
                );
    }

    private String nextEmpCode() {

        long n = repository.count() + 1;

        return String.format(
                "NEX-%04d",
                n
        );
    }
}
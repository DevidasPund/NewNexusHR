package com.nexushr.employee;

import com.nexushr.common.enums.EmployeeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository
        extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Employee> findByDepartment_Id(
            Long departmentId
    );

    List<Employee> findByStatus(
            EmployeeStatus status
    );

    long countByStatus(
            EmployeeStatus status
    );

    long countByDepartment_Id(
            Long departmentId
    );
}
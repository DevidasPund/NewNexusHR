package com.nexushr.payroll;

import com.nexushr.common.enums.PayslipStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PayslipRepository extends JpaRepository<Payslip, Long> {

    List<Payslip> findByEmployee_IdOrderByPeriodMonthDesc(Long employeeId);

    List<Payslip> findByPeriodMonthOrderByEmployee_FirstNameAsc(String periodMonth);

    Optional<Payslip> findByEmployee_IdAndPeriodMonth(Long employeeId, String periodMonth);

    boolean existsByEmployee_IdAndPeriodMonth(Long employeeId, String periodMonth);

    long countByPeriodMonth(String periodMonth);
}

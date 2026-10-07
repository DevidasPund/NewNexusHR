package com.nexushr.leave;

import com.nexushr.common.enums.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {

    List<LeaveBalance> findByEmployee_Id(Long employeeId);

    Optional<LeaveBalance> findByEmployee_IdAndType(Long employeeId, LeaveType type);
}

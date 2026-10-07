package com.nexushr.leave;

import com.nexushr.common.enums.LeaveStatus;
import com.nexushr.common.enums.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByEmployee_IdOrderByStartDateDesc(Long employeeId);

    List<LeaveRequest> findByStatusOrderByStartDateDesc(LeaveStatus status);

    List<LeaveRequest> findAllByOrderByStartDateDesc();

    long countByStatus(LeaveStatus status);

    long countByEmployee_IdAndStatus(Long employeeId, LeaveStatus status);

    long countByEmployee_IdAndTypeAndStatus(Long employeeId, LeaveType type, LeaveStatus status);
}

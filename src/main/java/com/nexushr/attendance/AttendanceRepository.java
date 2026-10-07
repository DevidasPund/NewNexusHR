package com.nexushr.attendance;

import com.nexushr.common.enums.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository
        extends JpaRepository<Attendance, Long> {

    Optional<Attendance>
    findByEmployee_IdAndDate(
            Long employeeId,
            LocalDate date
    );

    List<Attendance>
    findByEmployee_IdAndDateBetweenOrderByDateAsc(
            Long employeeId,
            LocalDate from,
            LocalDate to
    );

    List<Attendance>
    findByDate(LocalDate date);

    long countByDateAndStatus(
            LocalDate date,
            AttendanceStatus status
    );

    long countByDateAndStatusIn(
            LocalDate date,
            List<AttendanceStatus> statuses
    );
}
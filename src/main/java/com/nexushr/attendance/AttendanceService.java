package com.nexushr.attendance;

import com.nexushr.attendance.dto.AttendanceDto;
import com.nexushr.attendance.dto.AttendanceSummary;
import com.nexushr.common.enums.AttendanceStatus;
import com.nexushr.common.enums.EmployeeStatus;
import com.nexushr.common.exception.BadRequestException;
import com.nexushr.employee.Employee;
import com.nexushr.employee.EmployeeRepository;
import com.nexushr.employee.EmployeeService;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    /*
     * Employee arriving after 9:30 AM
     * will be marked as LATE.
     */
    private static final LocalTime LATE_AFTER =
            LocalTime.of(9, 30);

    private final AttendanceRepository repository;
    private final EmployeeService employeeService;
    private final EmployeeRepository employeeRepository;

    public AttendanceService(
            AttendanceRepository repository,
            EmployeeService employeeService,
            EmployeeRepository employeeRepository) {

        this.repository = repository;
        this.employeeService = employeeService;
        this.employeeRepository = employeeRepository;
    }

    // =========================================================
    // EMPLOYEE CLOCK IN
    // =========================================================

    public AttendanceDto clockIn(Long employeeId) {

        LocalDate today = LocalDate.now();

        /*
         * Check whether today's attendance already exists.
         */
        Attendance attendance =
                repository
                        .findByEmployee_IdAndDate(
                                employeeId,
                                today
                        )
                        .orElse(null);

        /*
         * Prevent duplicate clock-in.
         */
        if (attendance != null &&
                attendance.getClockIn() != null) {

            throw new BadRequestException(
                    "You have already clocked in today"
            );
        }

        /*
         * Get employee from database.
         */
        Employee employee =
                employeeService.getEntity(employeeId);

        /*
         * Only active employees can mark attendance.
         */
        if (employee.getStatus() != EmployeeStatus.ACTIVE) {

            throw new BadRequestException(
                    "Inactive employee cannot mark attendance"
            );
        }

        /*
         * If today's attendance does not exist,
         * create a new attendance record.
         */
        if (attendance == null) {

            attendance = new Attendance(
                    employee,
                    today,
                    AttendanceStatus.PRESENT
            );
        }

        /*
         * Get current server time.
         */
        LocalTime now =
                LocalTime.now()
                        .withSecond(0)
                        .withNano(0);

        /*
         * Save clock-in time.
         */
        attendance.setClockIn(now);

        /*
         * Automatically determine PRESENT/LATE.
         */
        if (now.isAfter(LATE_AFTER)) {

            attendance.setStatus(
                    AttendanceStatus.LATE
            );

        } else {

            attendance.setStatus(
                    AttendanceStatus.PRESENT
            );
        }

        /*
         * Save to MySQL.
         */
        Attendance saved =
                repository.save(attendance);

        return AttendanceDto.from(saved);
    }

    // =========================================================
    // EMPLOYEE CLOCK OUT
    // =========================================================

    public AttendanceDto clockOut(Long employeeId) {

        LocalDate today = LocalDate.now();

        /*
         * Find today's attendance.
         */
        Attendance attendance =
                repository
                        .findByEmployee_IdAndDate(
                                employeeId,
                                today
                        )
                        .orElseThrow(
                                () -> new BadRequestException(
                                        "Clock in before clocking out"
                                )
                        );

        /*
         * Employee must clock in first.
         */
        if (attendance.getClockIn() == null) {

            throw new BadRequestException(
                    "Clock in before clocking out"
            );
        }

        /*
         * Prevent duplicate clock-out.
         */
        if (attendance.getClockOut() != null) {

            throw new BadRequestException(
                    "You have already clocked out today"
            );
        }

        /*
         * Current time.
         */
        LocalTime now =
                LocalTime.now()
                        .withSecond(0)
                        .withNano(0);

        /*
         * Save clock-out.
         */
        attendance.setClockOut(now);

        /*
         * Calculate worked hours.
         */
        double hours =
                Duration
                        .between(
                                attendance.getClockIn(),
                                now
                        )
                        .toMinutes()
                        / 60.0;

        /*
         * Handle invalid time situation.
         */
        if (hours < 0) {
            throw new BadRequestException(
                    "Clock out time cannot be before clock in time"
            );
        }

        /*
         * Round to 2 decimal places.
         */
        double roundedHours =
                Math.round(hours * 100.0) / 100.0;

        attendance.setWorkedHours(
                roundedHours
        );

        /*
         * Save to MySQL.
         */
        Attendance saved =
                repository.save(attendance);

        return AttendanceDto.from(saved);
    }

    // =========================================================
    // TODAY'S ATTENDANCE FOR LOGGED-IN EMPLOYEE
    // =========================================================

    public AttendanceDto getToday(Long employeeId) {

        return repository
                .findByEmployee_IdAndDate(
                        employeeId,
                        LocalDate.now()
                )
                .map(AttendanceDto::from)
                .orElse(null);
    }

    // =========================================================
    // EMPLOYEE MONTHLY ATTENDANCE
    // =========================================================

    public List<AttendanceDto> monthly(
            Long employeeId,
            YearMonth yearMonth) {

        return repository
                .findByEmployee_IdAndDateBetweenOrderByDateAsc(
                        employeeId,
                        yearMonth.atDay(1),
                        yearMonth.atEndOfMonth()
                )
                .stream()
                .map(AttendanceDto::from)
                .toList();
    }

    // =========================================================
    // MANAGER / ADMIN - TODAY'S ALL EMPLOYEES
    // =========================================================

    public List<AttendanceDto> byDate(LocalDate date) {

        /*
         * Get all ACTIVE employees.
         */
        List<Employee> employees =
                employeeRepository.findByStatus(
                        EmployeeStatus.ACTIVE
                );

        /*
         * Get attendance records for selected date.
         */
        List<Attendance> attendanceRecords =
                repository.findByDate(date);

        /*
         * Convert attendance list into Map:
         *
         * employeeId -> Attendance
         */
        Map<Long, Attendance> attendanceMap =
                attendanceRecords
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        a -> a.getEmployee().getId(),
                                        Function.identity()
                                )
                        );

        /*
         * Build manager's attendance list.
         */
        return employees
                .stream()
                .map(employee -> {

                    Attendance attendance =
                            attendanceMap.get(
                                    employee.getId()
                            );

                    /*
                     * Employee has attendance record.
                     */
                    if (attendance != null) {

                        return AttendanceDto.from(
                                attendance
                        );
                    }

                    /*
                     * Employee has NOT clocked in.
                     *
                     * Create an in-memory DTO showing ABSENT.
                     *
                     * It is NOT saved to database.
                     */
                    return new AttendanceDto(
                            null,
                            employee.getId(),
                            employee.getFullName(),
                            employee.getEmpCode(),
                            employee.getAvatarColor(),
                            date,
                            null,
                            null,
                            AttendanceStatus.ABSENT,
                            0.0
                    );

                })
                .sorted(
                        Comparator.comparing(
                                AttendanceDto::employeeName,
                                String.CASE_INSENSITIVE_ORDER
                        )
                )
                .toList();
    }

    // =========================================================
    // EMPLOYEE SUMMARY
    // =========================================================

    public AttendanceSummary summary(
            Long employeeId,
            YearMonth yearMonth) {

        List<Attendance> records =
                repository
                        .findByEmployee_IdAndDateBetweenOrderByDateAsc(
                                employeeId,
                                yearMonth.atDay(1),
                                yearMonth.atEndOfMonth()
                        );

        int present = 0;
        int late = 0;
        int wfh = 0;
        int half = 0;
        int absent = 0;
        int leave = 0;

        double hoursSum = 0;
        int hoursCount = 0;

        /*
         * Count attendance statuses.
         */
        for (Attendance attendance : records) {

            if (attendance.getStatus() == null) {
                continue;
            }

            switch (attendance.getStatus()) {

                case PRESENT:
                    present++;
                    break;

                case LATE:
                    late++;
                    break;

                case WFH:
                    wfh++;
                    break;

                case HALF_DAY:
                    half++;
                    break;

                case ABSENT:
                    absent++;
                    break;

                case LEAVE:
                    leave++;
                    break;
            }

            /*
             * Calculate average worked hours.
             */
            if (attendance.getWorkedHours() > 0) {

                hoursSum +=
                        attendance.getWorkedHours();

                hoursCount++;
            }
        }

        /*
         * Number of Monday-Friday days.
         */
        int workingDays =
                workingDays(yearMonth);

        /*
         * Calculate credited attendance.
         *
         * PRESENT = 1
         * LATE = 1
         * WFH = 1
         * HALF_DAY = 0.5
         */
        double credited =
                present
                        + late
                        + wfh
                        + (half * 0.5);

        /*
         * Attendance percentage.
         */
        double rate =
                workingDays == 0
                        ? 0
                        : Math.min(
                                100.0,
                                credited
                                        / workingDays
                                        * 100.0
                        );

        /*
         * Average worked hours.
         */
        double averageHours =
                hoursCount == 0
                        ? 0
                        : hoursSum / hoursCount;

        /*
         * Round percentage.
         */
        double roundedRate =
                Math.round(rate * 10.0) / 10.0;

        /*
         * Round average hours.
         */
        double roundedAverageHours =
                Math.round(
                        averageHours * 10.0
                ) / 10.0;

        return new AttendanceSummary(
                present,
                late,
                wfh,
                half,
                absent,
                leave,
                workingDays,
                roundedRate,
                roundedAverageHours
        );
    }

    // =========================================================
    // WORKING DAYS
    // =========================================================

    private int workingDays(YearMonth yearMonth) {

        int count = 0;

        for (
                int day = 1;
                day <= yearMonth.lengthOfMonth();
                day++
        ) {

            DayOfWeek dayOfWeek =
                    yearMonth
                            .atDay(day)
                            .getDayOfWeek();

            /*
             * Monday-Friday.
             */
            if (
                    dayOfWeek != DayOfWeek.SATURDAY
                            &&
                    dayOfWeek != DayOfWeek.SUNDAY
            ) {

                count++;
            }
        }

        return count;
    }
}
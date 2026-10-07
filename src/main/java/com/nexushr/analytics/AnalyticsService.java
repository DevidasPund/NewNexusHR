package com.nexushr.analytics;

import com.nexushr.analytics.dto.AttritionRisk;
import com.nexushr.analytics.dto.DashboardDto;
import com.nexushr.analytics.dto.HeadcountByDept;
import com.nexushr.attendance.AttendanceService;
import com.nexushr.attendance.dto.AttendanceSummary;
import com.nexushr.common.enums.AttendanceStatus;
import com.nexushr.common.enums.EmployeeStatus;
import com.nexushr.common.enums.LeaveStatus;
import com.nexushr.common.enums.RiskBand;
import com.nexushr.department.Department;
import com.nexushr.department.DepartmentRepository;
import com.nexushr.attendance.AttendanceRepository;
import com.nexushr.employee.Employee;
import com.nexushr.employee.EmployeeRepository;
import com.nexushr.leave.LeaveRequestRepository;
import com.nexushr.payroll.Payslip;
import com.nexushr.payroll.PayslipRepository;
import com.nexushr.performance.PerformanceReview;
import com.nexushr.performance.PerformanceService;
import com.nexushr.performance.dto.ScorePoint;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class AnalyticsService {

    private final EmployeeRepository employeeRepo;
    private final DepartmentRepository departmentRepo;
    private final AttendanceRepository attendanceRepo;
    private final LeaveRequestRepository leaveRepo;
    private final PayslipRepository payslipRepo;
    private final AttendanceService attendanceService;
    private final PerformanceService performanceService;

    public AnalyticsService(EmployeeRepository employeeRepo,
                            DepartmentRepository departmentRepo,
                            AttendanceRepository attendanceRepo,
                            LeaveRequestRepository leaveRepo,
                            PayslipRepository payslipRepo,
                            AttendanceService attendanceService,
                            PerformanceService performanceService) {
        this.employeeRepo = employeeRepo;
        this.departmentRepo = departmentRepo;
        this.attendanceRepo = attendanceRepo;
        this.leaveRepo = leaveRepo;
        this.payslipRepo = payslipRepo;
        this.attendanceService = attendanceService;
        this.performanceService = performanceService;
    }

    @Transactional(readOnly = true)
    public DashboardDto dashboard(YearMonth month) {
        LocalDate today = LocalDate.now();
        long headcount = employeeRepo.count();
        long active = employeeRepo.countByStatus(EmployeeStatus.ACTIVE);
        long onLeave = employeeRepo.countByStatus(EmployeeStatus.ON_LEAVE);

        long presentToday = attendanceRepo.countByDateAndStatusIn(today,
                List.of(AttendanceStatus.PRESENT, AttendanceStatus.LATE, AttendanceStatus.WFH));
        long pendingApprovals = leaveRepo.countByStatus(LeaveStatus.PENDING);

        // New hires this month
        YearMonth ym = month;
        long newHires = employeeRepo.findAll().stream()
                .filter(e -> e.getDateOfJoining() != null
                        && YearMonth.from(e.getDateOfJoining()).equals(ym))
                .count();

        // Payroll net for the month
        BigDecimal payrollNet = payslipRepo.findByPeriodMonthOrderByEmployee_FirstNameAsc(month.toString())
                .stream()
                .map(Payslip::getNetPay)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Average performance (latest published per employee)
        List<Employee> all = employeeRepo.findAll();
        int scoreSum = 0;
        int scored = 0;
        for (Employee e : all) {
            PerformanceReview latest = performanceService.latestPublished(e.getId());
            if (latest != null) {
                scoreSum += latest.getOverallScore();
                scored++;
            }
        }
        int avgPerformance = scored == 0 ? 0 : Math.round((float) scoreSum / scored);

        // Attrition band counts
        long high = 0, medium = 0, low = 0;
        for (AttritionRisk r : attritionBoard()) {
            switch (r.band()) {
                case HIGH -> high++;
                case MEDIUM -> medium++;
                case LOW -> low++;
            }
        }

        // Headcount by department
        List<HeadcountByDept> byDept = new ArrayList<>();
        for (Department d : departmentRepo.findAll()) {
            long count = employeeRepo.countByDepartment_Id(d.getId());
            byDept.add(new HeadcountByDept(d.getName(), d.getColorKey(), count));
        }
        byDept.sort(Comparator.comparingLong(HeadcountByDept::count).reversed());

        // Org score trend: average overall score by period across all reviews
        List<ScorePoint> trend = orgScoreTrend();

        return new DashboardDto(
                month.toString(),
                headcount,
                active,
                onLeave,
                presentToday,
                pendingApprovals,
                newHires,
                payrollNet,
                avgPerformance,
                high,
                medium,
                low,
                byDept,
                trend);
    }

    /**
     * Deterministic attrition-risk heuristic. Score 0-100, higher = more likely to leave.
     * Weighted signals:
     *   - low recent performance      (up to 35 pts)
     *   - high absence / low attendance(up to 25 pts)
     *   - tenure plateau (long tenure, no recent promotion) (up to 20 pts)
     *   - rejected-leave friction      (up to 20 pts)
     */
    @Transactional(readOnly = true)
    public List<AttritionRisk> attritionBoard() {
        List<AttritionRisk> out = new ArrayList<>();
        YearMonth thisMonth = YearMonth.now();
        for (Employee e : employeeRepo.findByStatus(EmployeeStatus.ACTIVE)) {
            out.add(riskFor(e, thisMonth));
        }
        out.sort(Comparator.comparingInt(AttritionRisk::score).reversed());
        return out;
    }

    private AttritionRisk riskFor(Employee e, YearMonth month) {
        List<String> factors = new ArrayList<>();
        int score = 0;

        // 1. Performance signal (35)
        PerformanceReview latest = performanceService.latestPublished(e.getId());
        int lastScore = latest != null ? latest.getOverallScore() : 70; // neutral default
        if (lastScore < 60) {
            score += 35;
            factors.add("Low recent performance");
        } else if (lastScore < 75) {
            score += 18;
            factors.add("Below-average performance");
        }

        // 2. Attendance signal (25)
        AttendanceSummary att = attendanceService.summary(e.getId(), month);
        int attendanceRate = (int) Math.round(att.attendanceRate());
        if (attendanceRate < 70) {
            score += 25;
            factors.add("High absenteeism");
        } else if (attendanceRate < 85) {
            score += 12;
            factors.add("Irregular attendance");
        }

        // 3. Tenure plateau (20)
        int tenureMonths = e.getDateOfJoining() == null ? 0
                : (int) ChronoUnit.MONTHS.between(e.getDateOfJoining(), LocalDate.now());
        int monthsSincePromo = e.getLastPromotionDate() == null ? tenureMonths
                : (int) ChronoUnit.MONTHS.between(e.getLastPromotionDate(), LocalDate.now());
        if (tenureMonths >= 24 && monthsSincePromo >= 18) {
            score += 20;
            factors.add("Tenure plateau (no recent promotion)");
        } else if (tenureMonths >= 12 && monthsSincePromo >= 12) {
            score += 10;
            factors.add("Awaiting career progression");
        }

        // 4. Rejected-leave friction (20)
        long rejected = leaveRepo.countByEmployee_IdAndStatus(e.getId(), LeaveStatus.REJECTED);
        if (rejected >= 2) {
            score += 20;
            factors.add("Repeated rejected leave requests");
        } else if (rejected == 1) {
            score += 8;
            factors.add("Recent rejected leave");
        }

        score = Math.min(100, score);
        RiskBand band = score >= 60 ? RiskBand.HIGH : score >= 35 ? RiskBand.MEDIUM : RiskBand.LOW;
        if (factors.isEmpty()) {
            factors.add("Stable indicators");
        }

        return new AttritionRisk(
                e.getId(),
                e.getFullName(),
                e.getEmpCode(),
                e.getAvatarColor(),
                e.getDepartment() != null ? e.getDepartment().getName() : null,
                e.getDesignation(),
                score,
                band,
                factors,
                tenureMonths,
                attendanceRate,
                lastScore);
    }

    /** Average overall score grouped by review period, ordered by first appearance. */
    private List<ScorePoint> orgScoreTrend() {
        var periods = new java.util.LinkedHashMap<String, int[]>(); // period -> [sum, count]
        for (Employee e : employeeRepo.findAll()) {
            for (PerformanceReview r : performanceService.reviewsFor(e.getId())) {
                int[] agg = periods.computeIfAbsent(r.getPeriod(), k -> new int[2]);
                agg[0] += r.getOverallScore();
                agg[1] += 1;
            }
        }
        List<ScorePoint> trend = new ArrayList<>();
        periods.forEach((period, agg) ->
                trend.add(new ScorePoint(period, agg[1] == 0 ? 0 : Math.round((float) agg[0] / agg[1]))));
        return trend;
    }
}

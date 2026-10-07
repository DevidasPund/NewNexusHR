package com.nexushr.bootstrap;

import com.nexushr.attendance.Attendance;
import com.nexushr.attendance.AttendanceRepository;
import com.nexushr.common.enums.AttendanceStatus;
import com.nexushr.common.enums.EmployeeStatus;
import com.nexushr.common.enums.LeaveStatus;
import com.nexushr.common.enums.LeaveType;
import com.nexushr.common.enums.NotificationType;
import com.nexushr.common.enums.ReviewStatus;
import com.nexushr.common.enums.Role;
import com.nexushr.department.Department;
import com.nexushr.department.DepartmentRepository;
import com.nexushr.employee.Employee;
import com.nexushr.employee.EmployeeRepository;
import com.nexushr.leave.LeaveBalance;
import com.nexushr.leave.LeaveBalanceRepository;
import com.nexushr.leave.LeaveRequest;
import com.nexushr.leave.LeaveRequestRepository;
import com.nexushr.notification.Notification;
import com.nexushr.notification.NotificationRepository;
import com.nexushr.payroll.PayrollService;
import com.nexushr.performance.PerformanceReview;
import com.nexushr.performance.PerformanceReviewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Seeds a realistic demo dataset the first time the app starts against an empty
 * database. Controlled by {@code app.seed.enabled}; idempotent (skips if employees exist).
 */
@Component
@Order(1)
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final DepartmentRepository departmentRepo;
    private final EmployeeRepository employeeRepo;
    private final AttendanceRepository attendanceRepo;
    private final LeaveRequestRepository leaveRepo;
    private final LeaveBalanceRepository balanceRepo;
    private final PerformanceReviewRepository reviewRepo;
    private final NotificationRepository notificationRepo;
    private final PayrollService payrollService;
    private final PasswordEncoder passwordEncoder;

    private final boolean seedEnabled;
    private final String seedPassword;

    private final Random rnd = new Random(42);

    public DataSeeder(DepartmentRepository departmentRepo,
                      EmployeeRepository employeeRepo,
                      AttendanceRepository attendanceRepo,
                      LeaveRequestRepository leaveRepo,
                      LeaveBalanceRepository balanceRepo,
                      PerformanceReviewRepository reviewRepo,
                      NotificationRepository notificationRepo,
                      PayrollService payrollService,
                      PasswordEncoder passwordEncoder,
                      @Value("${app.seed.enabled:true}") boolean seedEnabled,
                      @Value("${app.seed.password:Passw0rd!}") String seedPassword) {
        this.departmentRepo = departmentRepo;
        this.employeeRepo = employeeRepo;
        this.attendanceRepo = attendanceRepo;
        this.leaveRepo = leaveRepo;
        this.balanceRepo = balanceRepo;
        this.reviewRepo = reviewRepo;
        this.notificationRepo = notificationRepo;
        this.payrollService = payrollService;
        this.passwordEncoder = passwordEncoder;
        this.seedEnabled = seedEnabled;
        this.seedPassword = seedPassword;
    }

    @Override
    public void run(String... args) {
        if (!seedEnabled) {
            return;
        }
        if (employeeRepo.count() > 0) {
            log.info("NexusHR seed skipped — {} employees already present.", employeeRepo.count());
            return;
        }

        log.info("Seeding NexusHR demo data...");
        List<Department> depts = seedDepartments();
        List<Employee> employees = seedEmployees(depts);
        seedAttendance(employees);
        seedLeave(employees);
        seedPerformance(employees);
        seedPayroll();
        seedNotifications(employees);

        log.info("========================================================");
        log.info("NexusHR demo data ready. Demo logins (password: {}):", seedPassword);
        log.info("  ADMIN    -> admin@nexushr.io");
        log.info("  MANAGER  -> manager@nexushr.io");
        log.info("  EMPLOYEE -> employee@nexushr.io");
        log.info("========================================================");
    }

    private List<Department> seedDepartments() {
        List<Department> depts = List.of(
                new Department("Engineering", "ENG", "violet"),
                new Department("Product", "PRD", "blue"),
                new Department("Design", "DSG", "pink"),
                new Department("People Ops", "HR", "amber"),
                new Department("Sales", "SAL", "green"));
        return departmentRepo.saveAll(depts);
    }

    private List<Employee> seedEmployees(List<Department> depts) {
        Department eng = depts.get(0);
        Department prd = depts.get(1);
        Department dsg = depts.get(2);
        Department hr = depts.get(3);
        Department sal = depts.get(4);

        List<Employee> list = new ArrayList<>();
        int[] seq = {0};

        // Three fixed demo accounts
        list.add(build(seq, "Aarav", "Sharma", "admin@nexushr.io", Role.ADMIN, hr,
                "Head of People", new BigDecimal("2800000"), "Bengaluru", "violet",
                LocalDate.now().minusMonths(40), LocalDate.now().minusMonths(6),
                List.of("HR Strategy", "Compensation", "Leadership")));
        Employee admin = list.get(0);

        list.add(build(seq, "Meera", "Iyer", "manager@nexushr.io", Role.MANAGER, eng,
                "Engineering Manager", new BigDecimal("3200000"), "Bengaluru", "blue",
                LocalDate.now().minusMonths(30), LocalDate.now().minusMonths(4),
                List.of("Java", "System Design", "Mentoring")));
        Employee mgr = list.get(1);

        list.add(build(seq, "Rohan", "Verma", "employee@nexushr.io", Role.EMPLOYEE, eng,
                "Software Engineer", new BigDecimal("1800000"), "Remote", "green",
                LocalDate.now().minusMonths(14), null,
                List.of("Java", "Spring Boot", "React")));

        // Engineering
        list.add(build(seq, "Priya", "Nair", "priya.nair@nexushr.io", Role.EMPLOYEE, eng,
                "Senior Software Engineer", new BigDecimal("2400000"), "Bengaluru", "violet",
                LocalDate.now().minusMonths(28), LocalDate.now().minusMonths(20),
                List.of("Kotlin", "Microservices", "AWS")));
        list.add(build(seq, "Karthik", "Reddy", "karthik.reddy@nexushr.io", Role.EMPLOYEE, eng,
                "Software Engineer", new BigDecimal("1600000"), "Hyderabad", "blue",
                LocalDate.now().minusMonths(9), null,
                List.of("Python", "Django", "Postgres")));
        list.add(build(seq, "Sneha", "Gupta", "sneha.gupta@nexushr.io", Role.EMPLOYEE, eng,
                "QA Engineer", new BigDecimal("1400000"), "Pune", "pink",
                LocalDate.now().minusMonths(38), LocalDate.now().minusMonths(30),
                List.of("Selenium", "JUnit", "Cypress")));
        list.add(build(seq, "Arjun", "Mehta", "arjun.mehta@nexushr.io", Role.EMPLOYEE, eng,
                "DevOps Engineer", new BigDecimal("2000000"), "Remote", "amber",
                LocalDate.now().minusMonths(18), null,
                List.of("Kubernetes", "Terraform", "CI/CD")));

        // Product
        list.add(build(seq, "Ananya", "Rao", "ananya.rao@nexushr.io", Role.MANAGER, prd,
                "Product Manager", new BigDecimal("3000000"), "Bengaluru", "blue",
                LocalDate.now().minusMonths(26), LocalDate.now().minusMonths(10),
                List.of("Roadmapping", "Analytics", "Discovery")));
        list.add(build(seq, "Vikram", "Singh", "vikram.singh@nexushr.io", Role.EMPLOYEE, prd,
                "Associate PM", new BigDecimal("1700000"), "Delhi", "violet",
                LocalDate.now().minusMonths(11), null,
                List.of("User Research", "SQL", "Figma")));
        list.add(build(seq, "Divya", "Menon", "divya.menon@nexushr.io", Role.EMPLOYEE, prd,
                "Business Analyst", new BigDecimal("1500000"), "Kochi", "green",
                LocalDate.now().minusMonths(33), LocalDate.now().minusMonths(28),
                List.of("Requirements", "Jira", "Wireframing")));

        // Design
        list.add(build(seq, "Ishaan", "Kapoor", "ishaan.kapoor@nexushr.io", Role.MANAGER, dsg,
                "Design Lead", new BigDecimal("2600000"), "Mumbai", "pink",
                LocalDate.now().minusMonths(29), LocalDate.now().minusMonths(9),
                List.of("Design Systems", "Figma", "Prototyping")));
        list.add(build(seq, "Tara", "Joshi", "tara.joshi@nexushr.io", Role.EMPLOYEE, dsg,
                "Product Designer", new BigDecimal("1600000"), "Mumbai", "violet",
                LocalDate.now().minusMonths(13), null,
                List.of("UI", "Interaction", "Motion")));
        list.add(build(seq, "Neel", "Shah", "neel.shah@nexushr.io", Role.EMPLOYEE, dsg,
                "UX Researcher", new BigDecimal("1450000"), "Remote", "blue",
                LocalDate.now().minusMonths(7), null,
                List.of("Research", "Usability", "Surveys")));

        // People Ops
        list.add(build(seq, "Kavya", "Pillai", "kavya.pillai@nexushr.io", Role.EMPLOYEE, hr,
                "HR Business Partner", new BigDecimal("1550000"), "Bengaluru", "amber",
                LocalDate.now().minusMonths(22), LocalDate.now().minusMonths(16),
                List.of("Employee Relations", "Onboarding")));
        list.add(build(seq, "Rahul", "Bose", "rahul.bose@nexushr.io", Role.EMPLOYEE, hr,
                "Recruiter", new BigDecimal("1300000"), "Delhi", "green",
                LocalDate.now().minusMonths(5), null,
                List.of("Sourcing", "Interviewing")));

        // Sales
        list.add(build(seq, "Aisha", "Khan", "aisha.khan@nexushr.io", Role.MANAGER, sal,
                "Sales Manager", new BigDecimal("2900000"), "Mumbai", "green",
                LocalDate.now().minusMonths(34), LocalDate.now().minusMonths(11),
                List.of("Enterprise Sales", "Negotiation")));
        list.add(build(seq, "Dev", "Malhotra", "dev.malhotra@nexushr.io", Role.EMPLOYEE, sal,
                "Account Executive", new BigDecimal("1600000"), "Gurgaon", "violet",
                LocalDate.now().minusMonths(20), null,
                List.of("Prospecting", "Demos", "CRM")));
        list.add(build(seq, "Nisha", "Agarwal", "nisha.agarwal@nexushr.io", Role.EMPLOYEE, sal,
                "SDR", new BigDecimal("1100000"), "Gurgaon", "pink",
                LocalDate.now().minusMonths(4), null,
                List.of("Outreach", "Qualification")));
        list.add(build(seq, "Farhan", "Ali", "farhan.ali@nexushr.io", Role.EMPLOYEE, sal,
                "Account Executive", new BigDecimal("1650000"), "Mumbai", "blue",
                LocalDate.now().minusMonths(27), LocalDate.now().minusMonths(25),
                List.of("Closing", "Upsell")));

        // A couple more engineers to round out headcount
        list.add(build(seq, "Ria", "Chopra", "ria.chopra@nexushr.io", Role.EMPLOYEE, eng,
                "Frontend Engineer", new BigDecimal("1750000"), "Remote", "amber",
                LocalDate.now().minusMonths(16), null,
                List.of("React", "TypeScript", "CSS")));
        list.add(build(seq, "Sahil", "Kulkarni", "sahil.kulkarni@nexushr.io", Role.EMPLOYEE, eng,
                "Data Engineer", new BigDecimal("1900000"), "Pune", "green",
                LocalDate.now().minusMonths(23), LocalDate.now().minusMonths(19),
                List.of("Spark", "Airflow", "SQL")));
        list.add(build(seq, "Pooja", "Desai", "pooja.desai@nexushr.io", Role.EMPLOYEE, prd,
                "Product Analyst", new BigDecimal("1500000"), "Bengaluru", "pink",
                LocalDate.now().minusMonths(8), null,
                List.of("Amplitude", "SQL", "A/B Testing")));

        List<Employee> saved = employeeRepo.saveAll(list);

        // Assign managers + department heads
        Employee savedMgr = saved.get(1);
        Employee savedAdmin = saved.get(0);
        for (Employee e : saved) {
            if (e.getRole() == Role.EMPLOYEE && !e.getId().equals(savedMgr.getId())) {
                e.setManagerId(savedMgr.getId());
            }
        }
        // Mark one employee ON_LEAVE for realism
        saved.get(5).setStatus(EmployeeStatus.ON_LEAVE);
        employeeRepo.saveAll(saved);

        // Department heads
        depts.get(0).setHeadId(savedMgr.getId());
        depts.get(3).setHeadId(savedAdmin.getId());
        departmentRepo.saveAll(depts);

        log.info("Seeded {} employees across {} departments.", saved.size(), depts.size());
        return saved;
    }

    private Employee build(int[] seq, String first, String last, String email, Role role,
                           Department dept, String designation, BigDecimal salary,
                           String location, String color, LocalDate doj, LocalDate lastPromo,
                           List<String> skills) {
        Employee e = new Employee();
        e.setEmpCode(String.format("NEX-%04d", ++seq[0]));
        e.setFirstName(first);
        e.setLastName(last);
        e.setEmail(email);
        e.setPasswordHash(passwordEncoder.encode(seedPassword));
        e.setRole(role);
        e.setStatus(EmployeeStatus.ACTIVE);
        e.setDepartment(dept);
        e.setDesignation(designation);
        e.setBaseSalary(salary);
        e.setLocation(location);
        e.setAvatarColor(color);
        e.setDateOfJoining(doj);
        e.setLastPromotionDate(lastPromo);
        e.setEmploymentType("Full-time");
        e.setSkills(new ArrayList<>(skills));
        e.setPhone(String.format("+91 9%09d", rnd.nextInt(1_000_000_000)));
        return e;
    }

    private void seedAttendance(List<Employee> employees) {
        LocalDate today = LocalDate.now();
        List<Attendance> batch = new ArrayList<>();
        for (Employee e : employees) {
            for (int back = 0; back < 30; back++) {
                LocalDate day = today.minusDays(back);
                DayOfWeek dow = day.getDayOfWeek();
                if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
                    continue;
                }
                int roll = rnd.nextInt(100);
                AttendanceStatus status;
                if (roll < 70) status = AttendanceStatus.PRESENT;
                else if (roll < 82) status = AttendanceStatus.WFH;
                else if (roll < 90) status = AttendanceStatus.LATE;
                else if (roll < 94) status = AttendanceStatus.HALF_DAY;
                else if (roll < 97) status = AttendanceStatus.LEAVE;
                else status = AttendanceStatus.ABSENT;

                Attendance a = new Attendance(e, day, status);
                if (status == AttendanceStatus.PRESENT || status == AttendanceStatus.WFH
                        || status == AttendanceStatus.LATE || status == AttendanceStatus.HALF_DAY) {
                    int inHour = status == AttendanceStatus.LATE ? 10 : 9;
                    int inMin = rnd.nextInt(40);
                    LocalTime in = LocalTime.of(inHour, inMin);
                    a.setClockIn(in);
                    double hours = status == AttendanceStatus.HALF_DAY ? 4 + rnd.nextInt(2)
                            : 8 + rnd.nextInt(2);
                    LocalTime out = in.plusMinutes((long) (hours * 60));
                    a.setClockOut(out);
                    a.setWorkedHours(Math.round(hours * 100.0) / 100.0);
                }
                batch.add(a);
            }
        }
        attendanceRepo.saveAll(batch);
        log.info("Seeded {} attendance records.", batch.size());
    }

    private void seedLeave(List<Employee> employees) {
        List<LeaveBalance> balances = new ArrayList<>();
        List<LeaveRequest> requests = new ArrayList<>();
        LeaveType[] types = LeaveType.values();

        for (Employee e : employees) {
            balances.add(new LeaveBalance(e, LeaveType.CASUAL, 12));
            balances.add(new LeaveBalance(e, LeaveType.SICK, 10));
            balances.add(new LeaveBalance(e, LeaveType.EARNED, 15));
            balances.add(new LeaveBalance(e, LeaveType.UNPAID, 0));

            int count = rnd.nextInt(3); // 0-2 requests each
            for (int i = 0; i < count; i++) {
                LeaveType type = types[rnd.nextInt(3)]; // skip UNPAID mostly
                int startOffset = 1 + rnd.nextInt(40) - 20; // near today
                LocalDate start = LocalDate.now().plusDays(startOffset);
                int len = 1 + rnd.nextInt(3);
                LocalDate end = start.plusDays(len - 1);

                LeaveRequest r = new LeaveRequest();
                r.setEmployee(e);
                r.setType(type);
                r.setStartDate(start);
                r.setEndDate(end);
                r.setDays(len);
                r.setReason(sampleReason(type));

                int statusRoll = rnd.nextInt(100);
                if (statusRoll < 45) {
                    r.setStatus(LeaveStatus.APPROVED);
                    r.setApproverName("Meera Iyer");
                    // reflect usage in balance
                    for (LeaveBalance b : balances) {
                        if (b.getEmployee() == e && b.getType() == type) {
                            b.setUsed(b.getUsed() + len);
                        }
                    }
                } else if (statusRoll < 70) {
                    r.setStatus(LeaveStatus.PENDING);
                } else if (statusRoll < 85) {
                    r.setStatus(LeaveStatus.REJECTED);
                    r.setApproverName("Meera Iyer");
                } else {
                    r.setStatus(LeaveStatus.CANCELLED);
                }
                requests.add(r);
            }
        }
        balanceRepo.saveAll(balances);
        leaveRepo.saveAll(requests);
        log.info("Seeded {} leave balances and {} leave requests.", balances.size(), requests.size());
    }

    private String sampleReason(LeaveType type) {
        return switch (type) {
            case SICK -> "Not feeling well";
            case CASUAL -> "Personal work";
            case EARNED -> "Family vacation";
            case UNPAID -> "Extended personal leave";
        };
    }

    private void seedPerformance(List<Employee> employees) {
        String[] periods = {"Q2 2025", "Q3 2025", "Q4 2025"};
        List<PerformanceReview> reviews = new ArrayList<>();
        for (Employee e : employees) {
            // base competence per employee, drifting slightly each quarter
            int base = 62 + rnd.nextInt(30);
            for (int qi = 0; qi < periods.length; qi++) {
                int delivery = clamp(base + rnd.nextInt(12) - 6);
                int quality = clamp(base + rnd.nextInt(12) - 6);
                int collaboration = clamp(base + rnd.nextInt(12) - 6);
                int ownership = clamp(base + rnd.nextInt(12) - 6);
                int overall = Math.round((delivery + quality + collaboration + ownership) / 4.0f);

                PerformanceReview r = new PerformanceReview();
                r.setEmployee(e);
                r.setPeriod(periods[qi]);
                r.setDelivery(delivery);
                r.setQuality(quality);
                r.setCollaboration(collaboration);
                r.setOwnership(ownership);
                r.setOverallScore(overall);
                r.setGrade(PerformanceReview.gradeFor(overall));
                r.setStatus(ReviewStatus.PUBLISHED);
                r.setReviewerName("Meera Iyer");
                r.setReviewDate(LocalDate.now().minusMonths((periods.length - qi) * 3L));
                r.setComments(comment(overall));
                reviews.add(r);
                base += rnd.nextInt(6) - 2; // small drift
            }
        }
        reviewRepo.saveAll(reviews);
        log.info("Seeded {} performance reviews.", reviews.size());
    }

    private String comment(int score) {
        if (score >= 85) return "Consistently exceeds expectations; strong ownership.";
        if (score >= 70) return "Solid, reliable contributor meeting expectations.";
        if (score >= 60) return "Meets most goals; some areas to develop.";
        return "Below expectations this cycle; improvement plan advised.";
    }

    private void seedPayroll() {
        YearMonth current = YearMonth.now();
        for (int back = 2; back >= 0; back--) {
            YearMonth ym = current.minusMonths(back);
            payrollService.generateForMonth(ym);
        }
        log.info("Seeded payroll for last 3 months.");
    }

    private void seedNotifications(List<Employee> employees) {
        List<Notification> items = new ArrayList<>();
        items.add(new Notification(null, "Welcome to NexusHR",
                "Your workspace is ready. Explore the dashboard to get started.", NotificationType.INFO));
        items.add(new Notification(null, "Payroll processed",
                "Payslips for " + YearMonth.now() + " are now available.", NotificationType.PAYROLL));
        items.add(new Notification(null, "Q4 2025 reviews published",
                "Performance reviews for Q4 2025 are live.", NotificationType.PERFORMANCE));

        Employee employee = employees.get(2); // employee@nexushr.io
        items.add(new Notification(employee.getId(), "Leave update",
                "You have pending leave requests awaiting approval.", NotificationType.LEAVE));
        items.add(new Notification(employee.getId(), "Attendance reminder",
                "Don't forget to clock in before 9:30 AM.", NotificationType.WARNING));

        notificationRepo.saveAll(items);
        log.info("Seeded {} notifications.", items.size());
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(100, v));
    }
}

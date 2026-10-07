package com.nexushr.performance;

import com.nexushr.common.enums.ReviewStatus;
import com.nexushr.common.exception.ResourceNotFoundException;
import com.nexushr.employee.Employee;
import com.nexushr.employee.EmployeeService;
import com.nexushr.performance.dto.CreatePerformanceReviewRequest;
import com.nexushr.performance.dto.PerformanceOverview;
import com.nexushr.performance.dto.PerformanceReviewDto;
import com.nexushr.performance.dto.ScorePoint;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class PerformanceService {

    private final PerformanceReviewRepository reviewRepo;
    private final EmployeeService employeeService;

    public PerformanceService(PerformanceReviewRepository reviewRepo, EmployeeService employeeService) {
        this.reviewRepo = reviewRepo;
        this.employeeService = employeeService;
    }

    @Transactional(readOnly = true)
    public List<PerformanceReviewDto> listAll() {
        return reviewRepo.findAllByOrderByCreatedAtDesc().stream()
                .map(PerformanceReviewDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PerformanceReviewDto> listForEmployee(Long employeeId) {
        return reviewRepo.findByEmployee_IdOrderByCreatedAtAsc(employeeId).stream()
                .map(PerformanceReviewDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PerformanceReviewDto get(Long id) {
        return PerformanceReviewDto.from(load(id));
    }

    @Transactional
    public PerformanceReviewDto create(CreatePerformanceReviewRequest req, Long reviewerId) {
        Employee employee = employeeService.getEntity(req.employeeId());
        int overall = req.overallScore() != null
                ? clamp(req.overallScore())
                : Math.round((req.delivery() + req.quality() + req.collaboration() + req.ownership()) / 4.0f);

        PerformanceReview r = new PerformanceReview();
        r.setEmployee(employee);
        r.setPeriod(req.period());
        r.setDelivery(clamp(req.delivery()));
        r.setQuality(clamp(req.quality()));
        r.setCollaboration(clamp(req.collaboration()));
        r.setOwnership(clamp(req.ownership()));
        r.setOverallScore(overall);
        r.setGrade(PerformanceReview.gradeFor(overall));
        r.setComments(req.comments());
        r.setStatus(req.status() != null ? req.status() : ReviewStatus.PUBLISHED);
        r.setReviewDate(LocalDate.now());
        if (reviewerId != null) {
            Employee reviewer = employeeService.getEntity(reviewerId);
            r.setReviewerId(reviewerId);
            r.setReviewerName(reviewer.getFullName());
        }
        return PerformanceReviewDto.from(reviewRepo.save(r));
    }

    @Transactional(readOnly = true)
    public PerformanceOverview overview(Long employeeId) {
        Employee employee = employeeService.getEntity(employeeId);
        List<PerformanceReview> reviews =
                reviewRepo.findByEmployee_IdAndStatusOrderByCreatedAtAsc(employeeId, ReviewStatus.PUBLISHED);

        if (reviews.isEmpty()) {
            return new PerformanceOverview(employeeId, employee.getFullName(), false,
                    0, "-", 0, 0, 0, 0, 0, 0, 0, List.of());
        }

        PerformanceReview latest = reviews.get(reviews.size() - 1);
        int sum = 0;
        int best = 0;
        List<ScorePoint> trend = new java.util.ArrayList<>();
        for (PerformanceReview r : reviews) {
            sum += r.getOverallScore();
            best = Math.max(best, r.getOverallScore());
            trend.add(new ScorePoint(r.getPeriod(), r.getOverallScore()));
        }
        int average = Math.round((float) sum / reviews.size());

        return new PerformanceOverview(
                employeeId,
                employee.getFullName(),
                true,
                latest.getOverallScore(),
                latest.getGrade(),
                latest.getDelivery(),
                latest.getQuality(),
                latest.getCollaboration(),
                latest.getOwnership(),
                average,
                best,
                reviews.size(),
                trend);
    }

    /** All published review entities for an employee, oldest first. Used by analytics. */
    @Transactional(readOnly = true)
    public List<PerformanceReview> reviewsFor(Long employeeId) {
        return reviewRepo.findByEmployee_IdAndStatusOrderByCreatedAtAsc(employeeId, ReviewStatus.PUBLISHED);
    }

    /** Latest published review for an employee, or null. Used by analytics. */
    @Transactional(readOnly = true)
    public PerformanceReview latestPublished(Long employeeId) {
        List<PerformanceReview> reviews =
                reviewRepo.findByEmployee_IdAndStatusOrderByCreatedAtAsc(employeeId, ReviewStatus.PUBLISHED);
        return reviews.isEmpty() ? null : reviews.get(reviews.size() - 1);
    }

    private PerformanceReview load(Long id) {
        return reviewRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PerformanceReview", id));
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(100, v));
    }
}

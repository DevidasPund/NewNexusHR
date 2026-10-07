package com.nexushr.performance;

import com.nexushr.common.enums.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Long> {

    List<PerformanceReview> findByEmployee_IdOrderByCreatedAtAsc(Long employeeId);

    List<PerformanceReview> findByEmployee_IdAndStatusOrderByCreatedAtAsc(Long employeeId, ReviewStatus status);

    List<PerformanceReview> findByStatusOrderByCreatedAtDesc(ReviewStatus status);

    List<PerformanceReview> findAllByOrderByCreatedAtDesc();
}

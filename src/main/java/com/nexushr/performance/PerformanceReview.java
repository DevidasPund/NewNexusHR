package com.nexushr.performance;

import com.nexushr.common.BaseEntity;
import com.nexushr.common.enums.ReviewStatus;
import com.nexushr.employee.Employee;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "performance_reviews")
public class PerformanceReview extends BaseEntity {

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(nullable = false, length = 32)
    private String period; // e.g. "Q1 2026"

    private int overallScore; // 0-100

    @Column(length = 4)
    private String grade;

    private int delivery;
    private int quality;
    private int collaboration;
    private int ownership;

    private Long reviewerId;
    private String reviewerName;

    @Column(length = 1000)
    private String comments;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewStatus status = ReviewStatus.PUBLISHED;

    private LocalDate reviewDate;

    public PerformanceReview() {
    }

    /** Letter grade for a 0-100 score. */
    public static String gradeFor(int score) {
        if (score >= 90) return "A+";
        if (score >= 80) return "A";
        if (score >= 70) return "B";
        if (score >= 60) return "C";
        return "D";
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public int getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(int overallScore) {
        this.overallScore = overallScore;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public int getDelivery() {
        return delivery;
    }

    public void setDelivery(int delivery) {
        this.delivery = delivery;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public int getCollaboration() {
        return collaboration;
    }

    public void setCollaboration(int collaboration) {
        this.collaboration = collaboration;
    }

    public int getOwnership() {
        return ownership;
    }

    public void setOwnership(int ownership) {
        this.ownership = ownership;
    }

    public Long getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(Long reviewerId) {
        this.reviewerId = reviewerId;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public ReviewStatus getStatus() {
        return status;
    }

    public void setStatus(ReviewStatus status) {
        this.status = status;
    }

    public LocalDate getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(LocalDate reviewDate) {
        this.reviewDate = reviewDate;
    }
}

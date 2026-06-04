package com.teaching.common.entity;

import lombok.Data;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "course_selections", indexes = {
    @Index(name = "idx_student_id", columnList = "student_id"),
    @Index(name = "idx_status", columnList = "status")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_course_student", columnNames = {"course_id", "student_id"})
})
public class CourseSelection {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "course_id", nullable = false)
    private Long courseId;
    
    @Column(name = "student_id", nullable = false)
    private Long studentId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SelectionStatus status = SelectionStatus.selected;
    
    @Column(columnDefinition = "DECIMAL(5,2)")
    private BigDecimal grade;
    
    @Column(name = "selected_at", nullable = false)
    private LocalDateTime selectedAt;
    
    @Column(name = "dropped_at")
    private LocalDateTime droppedAt;
    
    @PrePersist
    protected void onCreate() {
        selectedAt = LocalDateTime.now();
    }
    
    public enum SelectionStatus {
        selected, dropped
    }
}

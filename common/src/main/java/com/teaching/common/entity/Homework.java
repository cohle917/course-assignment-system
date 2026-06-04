package com.teaching.common.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "homeworks", indexes = {
    @Index(name = "idx_course_id", columnList = "course_id"),
    @Index(name = "idx_deadline", columnList = "deadline"),
    @Index(name = "idx_status", columnList = "status")
})
public class Homework {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(name = "course_id", nullable = false)
    private Long courseId;
    
    @Column(name = "course_name", length = 100)
    private String courseName;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "max_score", columnDefinition = "INT DEFAULT 100")
    private Integer maxScore = 100;
    
    @Column(nullable = false)
    private LocalDateTime deadline;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private HomeworkStatus status = HomeworkStatus.active;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum HomeworkStatus {
        active, closed
    }
}

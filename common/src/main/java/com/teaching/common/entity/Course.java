package com.teaching.common.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "courses", indexes = {
    @Index(name = "idx_teacher_id", columnList = "teacher_id"),
    @Index(name = "idx_name", columnList = "name"),
    @Index(name = "idx_semester", columnList = "semester"),
    @Index(name = "idx_status", columnList = "status")
})
public class Course {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(unique = true, length = 50)
    private String code;
    
    @Column(name = "teacher_id", nullable = false)
    private Long teacherId;
    
    @Column(name = "teacher_name", length = 50)
    private String teacherName;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(columnDefinition = "INT DEFAULT 3")
    private Integer credit = 3;
    
    @Column(name = "max_students", columnDefinition = "INT DEFAULT 50")
    private Integer maxStudents = 50;
    
    @Column(name = "current_students", columnDefinition = "INT DEFAULT 0")
    private Integer currentStudents = 0;
    
    @Column(length = 20)
    private String semester;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CourseStatus status = CourseStatus.open;
    
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
    
    public enum CourseStatus {
        open, closed
    }
}

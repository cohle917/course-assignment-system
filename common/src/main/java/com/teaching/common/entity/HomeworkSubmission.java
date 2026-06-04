package com.teaching.common.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "homework_submissions", indexes = {
    @Index(name = "idx_student_id", columnList = "student_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_submit_time", columnList = "submit_time")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_homework_student", columnNames = {"homework_id", "student_id"})
})
public class HomeworkSubmission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "homework_id", nullable = false)
    private Long homeworkId;
    
    @Column(name = "student_id", nullable = false)
    private Long studentId;
    
    @Column(name = "student_name", length = 50)
    private String studentName;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @Column(name = "file_path", length = 255)
    private String filePath;
    
    @Column
    private Integer score;
    
    @Column(columnDefinition = "TEXT")
    private String comment;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubmissionStatus status = SubmissionStatus.submitted;
    
    @Column(name = "submit_time", nullable = false)
    private LocalDateTime submitTime;
    
    @Column(name = "graded_at")
    private LocalDateTime gradedAt;
    
    @PrePersist
    protected void onCreate() {
        submitTime = LocalDateTime.now();
    }
    
    public enum SubmissionStatus {
        submitted, graded
    }
}

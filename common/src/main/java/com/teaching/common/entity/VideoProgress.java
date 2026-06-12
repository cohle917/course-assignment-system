package com.teaching.common.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "video_progress", indexes = {
        @Index(name = "idx_progress_video_id", columnList = "video_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_progress_student_video", columnNames = {"student_id", "video_id"})
})
public class VideoProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "video_id", nullable = false)
    private Long videoId;

    @Column(name = "last_position", columnDefinition = "INT DEFAULT 0")
    private Integer lastPosition = 0;

    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer duration = 0;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

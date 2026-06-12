package com.teaching.common.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "course_videos", indexes = {
        @Index(name = "idx_video_course_id", columnList = "course_id"),
        @Index(name = "idx_video_chapter_id", columnList = "chapter_id"),
        @Index(name = "idx_video_status", columnList = "status"),
        @Index(name = "idx_video_sort", columnList = "chapter_id, sort_order")
})
public class CourseVideo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "chapter_id")
    private Long chapterId;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "video_url", nullable = false, length = 1000)
    private String videoUrl;

    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer duration = 0;

    @Column(name = "sort_order", columnDefinition = "INT DEFAULT 0")
    private Integer sortOrder = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VideoStatus status = VideoStatus.draft;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum VideoStatus {
        draft, published, hidden
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

package com.teaching.common.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "video_danmaku", indexes = {
        @Index(name = "idx_danmaku_video_time", columnList = "video_id, time_seconds"),
        @Index(name = "idx_danmaku_course_id", columnList = "course_id")
})
public class VideoDanmaku {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "video_id", nullable = false)
    private Long videoId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "student_name", length = 50)
    private String studentName;

    @Column(name = "time_seconds", columnDefinition = "INT DEFAULT 0")
    private Integer timeSeconds = 0;

    @Column(nullable = false, length = 200)
    private String content;

    @Column(length = 20)
    private String color = "#ffffff";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DanmakuStatus status = DanmakuStatus.visible;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum DanmakuStatus {
        visible, hidden
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

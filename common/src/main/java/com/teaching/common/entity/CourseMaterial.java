package com.teaching.common.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "course_materials", indexes = {
        @Index(name = "idx_material_course_id", columnList = "course_id"),
        @Index(name = "idx_material_chapter_id", columnList = "chapter_id"),
        @Index(name = "idx_material_sort", columnList = "course_id, sort_order")
})
public class CourseMaterial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "chapter_id")
    private Long chapterId;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(name = "file_type", length = 50)
    private String fileType;

    @Column(name = "resource_url", nullable = false, length = 1000)
    private String resourceUrl;

    @Column(name = "sort_order", columnDefinition = "INT DEFAULT 0")
    private Integer sortOrder = 0;

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
}

# Frontend Video Player Course Content Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build URL-backed course video learning with a frontend `VideoPlayer`, resume playback, playback speed controls, shared danmaku, chapter structure, courseware links, and announcements.

**Architecture:** Keep the feature inside the existing `common`, `course-service`, and `frontend` modules. The backend stores course content metadata, progress, and danmaku; the frontend renders teacher management screens and student playback through a reusable browser-video component.

**Tech Stack:** Java 11, Spring Boot 2.7, Spring Data JPA, MySQL, Vue 3 Options API, Element Plus, native HTML5 `<video>`.

---

## File Structure

### Backend Shared Entities

- Create `common/src/main/java/com/teaching/common/entity/CourseChapter.java`: chapter and child section rows.
- Create `common/src/main/java/com/teaching/common/entity/CourseVideo.java`: URL-backed lesson video metadata.
- Create `common/src/main/java/com/teaching/common/entity/CourseMaterial.java`: courseware URL metadata.
- Create `common/src/main/java/com/teaching/common/entity/CourseAnnouncement.java`: teacher announcements.
- Create `common/src/main/java/com/teaching/common/entity/VideoProgress.java`: per-student resume playback state.
- Create `common/src/main/java/com/teaching/common/entity/VideoDanmaku.java`: shared visible/hidden timeline comments.

### Course Service

- Create `course-service/src/main/java/com/teaching/course/repository/CourseChapterRepository.java`
- Create `course-service/src/main/java/com/teaching/course/repository/CourseVideoRepository.java`
- Create `course-service/src/main/java/com/teaching/course/repository/CourseMaterialRepository.java`
- Create `course-service/src/main/java/com/teaching/course/repository/CourseAnnouncementRepository.java`
- Create `course-service/src/main/java/com/teaching/course/repository/VideoProgressRepository.java`
- Create `course-service/src/main/java/com/teaching/course/repository/VideoDanmakuRepository.java`
- Modify `course-service/src/main/java/com/teaching/course/service/CourseService.java`: add content management, permission checks, progress upsert, danmaku validation.
- Modify `course-service/src/main/java/com/teaching/course/controller/CourseController.java`: add REST endpoints for content management and learning.

### Database

- Modify `sql/init.sql`: add six content tables after `course_selections` or after `courses`, with indexes and foreign keys.
- Modify `sql/migration_v2.sql`: add idempotent `CREATE TABLE IF NOT EXISTS` statements for existing deployments.

### Frontend

- Modify `frontend/src/api/index.js`: add course content, video, progress, and danmaku API methods.
- Create `frontend/src/components/VideoPlayer.vue`: reusable browser video player with speed controls and danmaku overlay.
- Modify `frontend/src/views/CourseDetail.vue`: add student learning tab and teacher content management tab.
- Modify `frontend/src/views/TeacherDashboard.vue`: add create/edit course management entry and dialog.

---

## Task 1: Backend Entity Classes

**Files:**
- Create: `common/src/main/java/com/teaching/common/entity/CourseChapter.java`
- Create: `common/src/main/java/com/teaching/common/entity/CourseVideo.java`
- Create: `common/src/main/java/com/teaching/common/entity/CourseMaterial.java`
- Create: `common/src/main/java/com/teaching/common/entity/CourseAnnouncement.java`
- Create: `common/src/main/java/com/teaching/common/entity/VideoProgress.java`
- Create: `common/src/main/java/com/teaching/common/entity/VideoDanmaku.java`

- [ ] **Step 1: Add `CourseChapter`**

Create `CourseChapter.java` with this structure:

```java
package com.teaching.common.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "course_chapters", indexes = {
        @Index(name = "idx_chapter_course_id", columnList = "course_id"),
        @Index(name = "idx_chapter_parent_id", columnList = "parent_id"),
        @Index(name = "idx_chapter_sort", columnList = "course_id, sort_order")
})
public class CourseChapter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(nullable = false, length = 100)
    private String title;

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
```

- [ ] **Step 2: Add `CourseVideo`**

Create `CourseVideo.java` with this structure:

```java
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
```

- [ ] **Step 3: Add material, announcement, progress, and danmaku entities**

Use the same timestamp pattern as `CourseVideo`. Include these exact fields:

```java
// CourseMaterial fields
private Long id;
private Long courseId;
private Long chapterId;
private String title;
private String fileType;
private String resourceUrl;
private Integer sortOrder = 0;
private LocalDateTime createdAt;
private LocalDateTime updatedAt;

// CourseAnnouncement fields
private Long id;
private Long courseId;
private String title;
private String content;
private LocalDateTime publishedAt;
private LocalDateTime createdAt;
private LocalDateTime updatedAt;

// VideoProgress fields
private Long id;
private Long studentId;
private Long videoId;
private Integer lastPosition = 0;
private Integer duration = 0;
private LocalDateTime updatedAt;

// VideoDanmaku fields
private Long id;
private Long courseId;
private Long videoId;
private Long studentId;
private String studentName;
private Integer timeSeconds = 0;
private String content;
private String color = "#ffffff";
private DanmakuStatus status = DanmakuStatus.visible;
private LocalDateTime createdAt;
```

Use these table names:

```java
@Table(name = "course_materials")
@Table(name = "course_announcements")
@Table(name = "video_progress", uniqueConstraints = {
    @UniqueConstraint(name = "uk_progress_student_video", columnNames = {"student_id", "video_id"})
})
@Table(name = "video_danmaku")
```

Define `VideoDanmaku.DanmakuStatus` as:

```java
public enum DanmakuStatus {
    visible, hidden
}
```

- [ ] **Step 4: Compile common**

Run:

```bash
mvn -pl common package -DskipTests
```

Expected: build succeeds and Lombok-generated getters/setters compile.

- [ ] **Step 5: Commit entity classes**

Run:

```bash
git add common/src/main/java/com/teaching/common/entity/CourseChapter.java common/src/main/java/com/teaching/common/entity/CourseVideo.java common/src/main/java/com/teaching/common/entity/CourseMaterial.java common/src/main/java/com/teaching/common/entity/CourseAnnouncement.java common/src/main/java/com/teaching/common/entity/VideoProgress.java common/src/main/java/com/teaching/common/entity/VideoDanmaku.java
git commit -m "feat: add course content entities"
```

Expected: commit contains only the six entity files.

---

## Task 2: Database Schema

**Files:**
- Modify: `sql/init.sql`
- Modify: `sql/migration_v2.sql`

- [ ] **Step 1: Add content tables to `sql/init.sql`**

Insert these tables after `course_selections`:

```sql
CREATE TABLE IF NOT EXISTS course_chapters (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    parent_id BIGINT NULL,
    title VARCHAR(100) NOT NULL,
    sort_order INT DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_chapter_course_id (course_id),
    INDEX idx_chapter_parent_id (parent_id),
    INDEX idx_chapter_sort (course_id, sort_order),
    CONSTRAINT fk_chapter_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程章节表';

CREATE TABLE IF NOT EXISTS course_videos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    chapter_id BIGINT NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    video_url VARCHAR(1000) NOT NULL,
    duration INT DEFAULT 0,
    sort_order INT DEFAULT 0,
    status ENUM('draft', 'published', 'hidden') NOT NULL DEFAULT 'draft',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_video_course_id (course_id),
    INDEX idx_video_chapter_id (chapter_id),
    INDEX idx_video_status (status),
    INDEX idx_video_sort (chapter_id, sort_order),
    CONSTRAINT fk_video_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    CONSTRAINT fk_video_chapter FOREIGN KEY (chapter_id) REFERENCES course_chapters(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程视频表';

CREATE TABLE IF NOT EXISTS course_materials (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    chapter_id BIGINT NULL,
    title VARCHAR(150) NOT NULL,
    file_type VARCHAR(50),
    resource_url VARCHAR(1000) NOT NULL,
    sort_order INT DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_material_course_id (course_id),
    INDEX idx_material_chapter_id (chapter_id),
    CONSTRAINT fk_material_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    CONSTRAINT fk_material_chapter FOREIGN KEY (chapter_id) REFERENCES course_chapters(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程课件表';

CREATE TABLE IF NOT EXISTS course_announcements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    content TEXT NOT NULL,
    published_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_announcement_course_id (course_id),
    INDEX idx_announcement_published_at (published_at),
    CONSTRAINT fk_announcement_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程公告表';

CREATE TABLE IF NOT EXISTS video_progress (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    video_id BIGINT NOT NULL,
    last_position INT DEFAULT 0,
    duration INT DEFAULT 0,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_progress_student_video (student_id, video_id),
    INDEX idx_progress_video_id (video_id),
    CONSTRAINT fk_progress_student FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_progress_video FOREIGN KEY (video_id) REFERENCES course_videos(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频学习进度表';

CREATE TABLE IF NOT EXISTS video_danmaku (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    video_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    student_name VARCHAR(50),
    time_seconds INT DEFAULT 0,
    content VARCHAR(200) NOT NULL,
    color VARCHAR(20) DEFAULT '#ffffff',
    status ENUM('visible', 'hidden') NOT NULL DEFAULT 'visible',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_danmaku_video_time (video_id, time_seconds),
    INDEX idx_danmaku_course_id (course_id),
    CONSTRAINT fk_danmaku_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    CONSTRAINT fk_danmaku_video FOREIGN KEY (video_id) REFERENCES course_videos(id) ON DELETE CASCADE,
    CONSTRAINT fk_danmaku_student FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频弹幕表';
```

- [ ] **Step 2: Add the same six `CREATE TABLE IF NOT EXISTS` statements to `sql/migration_v2.sql`**

Keep the statements idempotent. Do not drop or recreate existing tables.

- [ ] **Step 3: Commit database schema**

Run:

```bash
git add sql/init.sql sql/migration_v2.sql
git commit -m "feat: add course content schema"
```

Expected: commit contains only SQL schema changes.

---

## Task 3: Repositories

**Files:**
- Create: `course-service/src/main/java/com/teaching/course/repository/CourseChapterRepository.java`
- Create: `course-service/src/main/java/com/teaching/course/repository/CourseVideoRepository.java`
- Create: `course-service/src/main/java/com/teaching/course/repository/CourseMaterialRepository.java`
- Create: `course-service/src/main/java/com/teaching/course/repository/CourseAnnouncementRepository.java`
- Create: `course-service/src/main/java/com/teaching/course/repository/VideoProgressRepository.java`
- Create: `course-service/src/main/java/com/teaching/course/repository/VideoDanmakuRepository.java`

- [ ] **Step 1: Add repository interfaces**

Use this pattern:

```java
package com.teaching.course.repository;

import com.teaching.common.entity.CourseChapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseChapterRepository extends JpaRepository<CourseChapter, Long> {
    List<CourseChapter> findByCourseIdOrderBySortOrderAscIdAsc(Long courseId);
    List<CourseChapter> findByCourseIdAndParentIdOrderBySortOrderAscIdAsc(Long courseId, Long parentId);
}
```

Add the remaining repositories with these methods:

```java
// CourseVideoRepository
List<CourseVideo> findByCourseIdOrderBySortOrderAscIdAsc(Long courseId);
List<CourseVideo> findByChapterIdOrderBySortOrderAscIdAsc(Long chapterId);
Optional<CourseVideo> findByIdAndCourseId(Long id, Long courseId);

// CourseMaterialRepository
List<CourseMaterial> findByCourseIdOrderBySortOrderAscIdAsc(Long courseId);
List<CourseMaterial> findByChapterIdOrderBySortOrderAscIdAsc(Long chapterId);

// CourseAnnouncementRepository
List<CourseAnnouncement> findByCourseIdOrderByPublishedAtDesc(Long courseId);

// VideoProgressRepository
Optional<VideoProgress> findByStudentIdAndVideoId(Long studentId, Long videoId);

// VideoDanmakuRepository
List<VideoDanmaku> findByVideoIdAndStatusOrderByTimeSecondsAscCreatedAtAsc(Long videoId, VideoDanmaku.DanmakuStatus status);
```

- [ ] **Step 2: Compile course service**

Run:

```bash
mvn -pl common,course-service -am package -DskipTests
```

Expected: build succeeds and repositories resolve entity classes.

- [ ] **Step 3: Commit repositories**

Run:

```bash
git add course-service/src/main/java/com/teaching/course/repository/CourseChapterRepository.java course-service/src/main/java/com/teaching/course/repository/CourseVideoRepository.java course-service/src/main/java/com/teaching/course/repository/CourseMaterialRepository.java course-service/src/main/java/com/teaching/course/repository/CourseAnnouncementRepository.java course-service/src/main/java/com/teaching/course/repository/VideoProgressRepository.java course-service/src/main/java/com/teaching/course/repository/VideoDanmakuRepository.java
git commit -m "feat: add course content repositories"
```

Expected: commit contains only repository files.

---

## Task 4: Course Service Content Methods

**Files:**
- Modify: `course-service/src/main/java/com/teaching/course/service/CourseService.java`

- [ ] **Step 1: Inject repositories**

Add constructor parameters and fields:

```java
private final CourseChapterRepository courseChapterRepository;
private final CourseVideoRepository courseVideoRepository;
private final CourseMaterialRepository courseMaterialRepository;
private final CourseAnnouncementRepository courseAnnouncementRepository;
private final VideoProgressRepository videoProgressRepository;
private final VideoDanmakuRepository videoDanmakuRepository;
```

Update the constructor to assign all six fields.

- [ ] **Step 2: Add permission helpers**

Add these methods near existing private helpers:

```java
private Course requireCourse(Long courseId) {
    return courseRepository.findById(courseId)
            .orElseThrow(() -> new RuntimeException("课程不存在"));
}

private CourseVideo requireVideo(Long videoId) {
    return courseVideoRepository.findById(videoId)
            .orElseThrow(() -> new RuntimeException("视频不存在"));
}

private void requireTeacherOwnsCourse(Long courseId, Long teacherId) {
    Course course = requireCourse(courseId);
    if (teacherId == null || !teacherId.equals(course.getTeacherId())) {
        throw new RuntimeException("无权管理该课程");
    }
}

private void requireStudentSelectedCourse(Long courseId, Long studentId) {
    if (studentId == null || !courseSelectionRepository.existsByCourseIdAndStudentId(courseId, studentId)) {
        throw new RuntimeException("未选该课程，无法访问学习内容");
    }
}

private void requireCourseAccess(Long courseId, Long userId, String role) {
    Course course = requireCourse(courseId);
    if ("teacher".equals(role) && userId != null && userId.equals(course.getTeacherId())) {
        return;
    }
    if ("student".equals(role)) {
        requireStudentSelectedCourse(courseId, userId);
        return;
    }
    throw new RuntimeException("无权访问该课程内容");
}
```

- [ ] **Step 3: Add URL validation**

Add:

```java
private void validateHttpUrl(String url, String fieldName) {
    if (url == null || url.trim().isEmpty()) {
        throw new RuntimeException(fieldName + "不能为空");
    }
    String normalized = url.trim().toLowerCase();
    if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
        throw new RuntimeException(fieldName + "必须是 http 或 https 地址");
    }
}
```

- [ ] **Step 4: Add content read method**

Add:

```java
public Map<String, Object> getLearningContent(Long courseId, Long userId, String role) {
    requireCourseAccess(courseId, userId, role);
    Map<String, Object> result = new HashMap<>();
    result.put("chapters", courseChapterRepository.findByCourseIdOrderBySortOrderAscIdAsc(courseId));
    result.put("videos", courseVideoRepository.findByCourseIdOrderBySortOrderAscIdAsc(courseId));
    result.put("materials", courseMaterialRepository.findByCourseIdOrderBySortOrderAscIdAsc(courseId));
    result.put("announcements", courseAnnouncementRepository.findByCourseIdOrderByPublishedAtDesc(courseId));
    return result;
}
```

- [ ] **Step 5: Add create/update/delete methods for chapters, materials, announcements, and videos**

Use these method signatures:

```java
@Transactional
public CourseChapter saveChapter(Long courseId, Long teacherId, CourseChapter chapter)

@Transactional
public CourseChapter updateChapter(Long chapterId, Long teacherId, CourseChapter input)

@Transactional
public void deleteChapter(Long chapterId, Long teacherId)

@Transactional
public CourseVideo saveVideo(Long courseId, Long teacherId, CourseVideo video)

@Transactional
public CourseVideo updateVideo(Long videoId, Long teacherId, CourseVideo input)

@Transactional
public void deleteVideo(Long videoId, Long teacherId)

@Transactional
public CourseMaterial saveMaterial(Long courseId, Long teacherId, CourseMaterial material)

@Transactional
public CourseMaterial updateMaterial(Long materialId, Long teacherId, CourseMaterial input)

@Transactional
public void deleteMaterial(Long materialId, Long teacherId)

@Transactional
public CourseAnnouncement saveAnnouncement(Long courseId, Long teacherId, CourseAnnouncement announcement)

@Transactional
public CourseAnnouncement updateAnnouncement(Long announcementId, Long teacherId, CourseAnnouncement input)

@Transactional
public void deleteAnnouncement(Long announcementId, Long teacherId)
```

Implementation rules:

```java
// saveVideo
requireTeacherOwnsCourse(courseId, teacherId);
validateHttpUrl(video.getVideoUrl(), "视频地址");
video.setCourseId(courseId);
if (video.getStatus() == null) video.setStatus(CourseVideo.VideoStatus.draft);
if (video.getSortOrder() == null) video.setSortOrder(0);
if (video.getDuration() == null) video.setDuration(0);
return courseVideoRepository.save(video);

// saveMaterial
requireTeacherOwnsCourse(courseId, teacherId);
validateHttpUrl(material.getResourceUrl(), "课件地址");
material.setCourseId(courseId);
if (material.getSortOrder() == null) material.setSortOrder(0);
return courseMaterialRepository.save(material);

// saveAnnouncement
requireTeacherOwnsCourse(courseId, teacherId);
announcement.setCourseId(courseId);
if (announcement.getPublishedAt() == null) announcement.setPublishedAt(LocalDateTime.now());
return courseAnnouncementRepository.save(announcement);
```

For delete methods, load the record, check ownership through its `courseId`, then call `repository.deleteById(id)` once for the explicit record.

- [ ] **Step 6: Add playback, progress, and danmaku methods**

Add:

```java
public Map<String, Object> getVideoPlayInfo(Long videoId, Long userId, String role) {
    CourseVideo video = requireVideo(videoId);
    requireCourseAccess(video.getCourseId(), userId, role);
    Map<String, Object> result = new HashMap<>();
    result.put("videoId", video.getId());
    result.put("courseId", video.getCourseId());
    result.put("title", video.getTitle());
    result.put("description", video.getDescription());
    result.put("videoUrl", video.getVideoUrl());
    result.put("duration", video.getDuration());
    return result;
}

public VideoProgress getVideoProgress(Long videoId, Long studentId) {
    CourseVideo video = requireVideo(videoId);
    requireStudentSelectedCourse(video.getCourseId(), studentId);
    return videoProgressRepository.findByStudentIdAndVideoId(studentId, videoId).orElse(null);
}

@Transactional
public VideoProgress saveVideoProgress(Long videoId, Long studentId, Integer lastPosition, Integer duration) {
    CourseVideo video = requireVideo(videoId);
    requireStudentSelectedCourse(video.getCourseId(), studentId);
    VideoProgress progress = videoProgressRepository.findByStudentIdAndVideoId(studentId, videoId)
            .orElseGet(VideoProgress::new);
    progress.setStudentId(studentId);
    progress.setVideoId(videoId);
    progress.setLastPosition(Math.max(0, lastPosition != null ? lastPosition : 0));
    progress.setDuration(Math.max(0, duration != null ? duration : 0));
    return videoProgressRepository.save(progress);
}
```

Add danmaku methods:

```java
public List<VideoDanmaku> getVideoDanmaku(Long videoId, Long userId, String role) {
    CourseVideo video = requireVideo(videoId);
    requireCourseAccess(video.getCourseId(), userId, role);
    return videoDanmakuRepository.findByVideoIdAndStatusOrderByTimeSecondsAscCreatedAtAsc(
            videoId, VideoDanmaku.DanmakuStatus.visible);
}

@Transactional
public VideoDanmaku addVideoDanmaku(Long videoId, VideoDanmaku danmaku) {
    CourseVideo video = requireVideo(videoId);
    requireStudentSelectedCourse(video.getCourseId(), danmaku.getStudentId());
    String content = danmaku.getContent() != null ? danmaku.getContent().trim() : "";
    if (content.isEmpty()) throw new RuntimeException("弹幕内容不能为空");
    if (content.length() > 200) throw new RuntimeException("弹幕内容不能超过200字");
    danmaku.setCourseId(video.getCourseId());
    danmaku.setVideoId(videoId);
    danmaku.setContent(content);
    if (danmaku.getTimeSeconds() == null || danmaku.getTimeSeconds() < 0) danmaku.setTimeSeconds(0);
    if (danmaku.getColor() == null || danmaku.getColor().trim().isEmpty()) danmaku.setColor("#ffffff");
    if (danmaku.getStatus() == null) danmaku.setStatus(VideoDanmaku.DanmakuStatus.visible);
    return videoDanmakuRepository.save(danmaku);
}
```

- [ ] **Step 7: Compile service**

Run:

```bash
mvn -pl common,course-service -am package -DskipTests
```

Expected: build succeeds.

- [ ] **Step 8: Commit service methods**

Run:

```bash
git add course-service/src/main/java/com/teaching/course/service/CourseService.java
git commit -m "feat: add course content service logic"
```

Expected: commit contains only `CourseService.java`.

---

## Task 5: Course Controller Endpoints

**Files:**
- Modify: `course-service/src/main/java/com/teaching/course/controller/CourseController.java`

- [ ] **Step 1: Add imports**

Add:

```java
import com.teaching.common.entity.CourseAnnouncement;
import com.teaching.common.entity.CourseChapter;
import com.teaching.common.entity.CourseMaterial;
import com.teaching.common.entity.CourseVideo;
import com.teaching.common.entity.VideoDanmaku;
import com.teaching.common.entity.VideoProgress;
```

- [ ] **Step 2: Add course update endpoint**

Add:

```java
@PutMapping("/{courseId}")
public Result<Course> updateCourse(@PathVariable Long courseId, @RequestBody Course input) {
    try {
        Course updated = courseService.updateCourse(courseId, input);
        return Result.success(updated);
    } catch (Exception e) {
        return Result.error(e.getMessage());
    }
}
```

Add `updateCourse` in `CourseService` if it does not exist:

```java
@Transactional
public Course updateCourse(Long courseId, Course input) {
    Course course = requireCourse(courseId);
    course.setName(input.getName());
    course.setCode(input.getCode());
    course.setDescription(input.getDescription());
    course.setCredit(input.getCredit());
    course.setMaxStudents(input.getMaxStudents());
    course.setCategory(input.getCategory());
    course.setDepartment(input.getDepartment());
    course.setCoverImage(input.getCoverImage());
    course.setSyllabus(input.getSyllabus());
    course.setSemester(input.getSemester());
    if (input.getStatus() != null) course.setStatus(input.getStatus());
    return courseRepository.save(course);
}
```

- [ ] **Step 3: Add learning content endpoint**

Add:

```java
@GetMapping("/{courseId}/learning-content")
public Result<Map<String, Object>> getLearningContent(
        @PathVariable Long courseId,
        @RequestParam Long userId,
        @RequestParam String role) {
    try {
        return Result.success(courseService.getLearningContent(courseId, userId, role));
    } catch (Exception e) {
        return Result.error(e.getMessage());
    }
}
```

- [ ] **Step 4: Add chapter endpoints**

Add:

```java
@PostMapping("/{courseId}/chapters")
public Result<CourseChapter> saveChapter(@PathVariable Long courseId, @RequestParam Long teacherId, @RequestBody CourseChapter chapter) {
    try {
        return Result.success(courseService.saveChapter(courseId, teacherId, chapter));
    } catch (Exception e) {
        return Result.error(e.getMessage());
    }
}

@PutMapping("/chapters/{chapterId}")
public Result<CourseChapter> updateChapter(@PathVariable Long chapterId, @RequestParam Long teacherId, @RequestBody CourseChapter chapter) {
    try {
        return Result.success(courseService.updateChapter(chapterId, teacherId, chapter));
    } catch (Exception e) {
        return Result.error(e.getMessage());
    }
}

@DeleteMapping("/chapters/{chapterId}")
public Result<Void> deleteChapter(@PathVariable Long chapterId, @RequestParam Long teacherId) {
    try {
        courseService.deleteChapter(chapterId, teacherId);
        return Result.success(null);
    } catch (Exception e) {
        return Result.error(e.getMessage());
    }
}
```

- [ ] **Step 5: Add video, material, announcement, progress, and danmaku endpoints**

Use the same try/catch `Result` pattern for these endpoints:

```java
POST /{courseId}/videos
PUT /videos/{videoId}
DELETE /videos/{videoId}
GET /videos/{videoId}/play-info
POST /{courseId}/materials
PUT /materials/{id}
DELETE /materials/{id}
POST /{courseId}/announcements
PUT /announcements/{id}
DELETE /announcements/{id}
GET /videos/{videoId}/progress
POST /videos/{videoId}/progress
GET /videos/{videoId}/danmaku
POST /videos/{videoId}/danmaku
```

Use request parameters:

```java
teacherId for teacher management endpoints
userId and role for play-info, learning-content, and danmaku reads
studentId for progress reads/writes and danmaku writes
```

For progress writes, read request body with:

```java
Integer lastPosition = request.get("lastPosition") == null ? 0 : Integer.valueOf(request.get("lastPosition").toString());
Integer duration = request.get("duration") == null ? 0 : Integer.valueOf(request.get("duration").toString());
```

- [ ] **Step 6: Compile controller**

Run:

```bash
mvn -pl common,course-service -am package -DskipTests
```

Expected: build succeeds.

- [ ] **Step 7: Commit controller endpoints**

Run:

```bash
git add course-service/src/main/java/com/teaching/course/controller/CourseController.java course-service/src/main/java/com/teaching/course/service/CourseService.java
git commit -m "feat: expose course content APIs"
```

Expected: commit contains controller changes and any service method added for course update.

---

## Task 6: Frontend API Methods

**Files:**
- Modify: `frontend/src/api/index.js`

- [ ] **Step 1: Add user helper**

Inside the default export object, before methods or as a module-level function, add:

```js
function getLocalUser() {
  return JSON.parse(localStorage.getItem('userInfo') || '{}')
}
```

If adding a module-level helper, place it above `export default`.

- [ ] **Step 2: Add course content API methods**

Add these methods to the exported API object:

```js
updateCourse(courseId, data) {
  return apiClient.put(`/course/${courseId}`, data)
},

getLearningContent(courseId) {
  const user = getLocalUser()
  return apiClient.get(`/course/${courseId}/learning-content`, {
    params: { userId: user.id, role: user.role }
  })
},

createChapter(courseId, data) {
  const user = getLocalUser()
  return apiClient.post(`/course/${courseId}/chapters`, data, {
    params: { teacherId: user.id }
  })
},

updateChapter(chapterId, data) {
  const user = getLocalUser()
  return apiClient.put(`/course/chapters/${chapterId}`, data, {
    params: { teacherId: user.id }
  })
},

deleteChapter(chapterId) {
  const user = getLocalUser()
  return apiClient.delete(`/course/chapters/${chapterId}`, {
    params: { teacherId: user.id }
  })
},
```

- [ ] **Step 3: Add video, material, announcement, progress, and danmaku API methods**

Add:

```js
createVideo(courseId, data) {
  const user = getLocalUser()
  return apiClient.post(`/course/${courseId}/videos`, data, {
    params: { teacherId: user.id }
  })
},

updateVideo(videoId, data) {
  const user = getLocalUser()
  return apiClient.put(`/course/videos/${videoId}`, data, {
    params: { teacherId: user.id }
  })
},

deleteVideo(videoId) {
  const user = getLocalUser()
  return apiClient.delete(`/course/videos/${videoId}`, {
    params: { teacherId: user.id }
  })
},

getVideoPlayInfo(videoId) {
  const user = getLocalUser()
  return apiClient.get(`/course/videos/${videoId}/play-info`, {
    params: { userId: user.id, role: user.role }
  })
},

saveVideoProgress(videoId, lastPosition, duration) {
  const user = getLocalUser()
  return apiClient.post(`/course/videos/${videoId}/progress`, {
    lastPosition,
    duration
  }, {
    params: { studentId: user.id }
  })
},

getVideoProgress(videoId) {
  const user = getLocalUser()
  return apiClient.get(`/course/videos/${videoId}/progress`, {
    params: { studentId: user.id }
  })
},

getVideoDanmaku(videoId) {
  const user = getLocalUser()
  return apiClient.get(`/course/videos/${videoId}/danmaku`, {
    params: { userId: user.id, role: user.role }
  })
},

sendVideoDanmaku(videoId, data) {
  const user = getLocalUser()
  return apiClient.post(`/course/videos/${videoId}/danmaku`, {
    ...data,
    studentId: user.id,
    studentName: user.name || user.username
  })
}
```

Add material methods:

```js
createMaterial(courseId, data) {
  const user = getLocalUser()
  return apiClient.post(`/course/${courseId}/materials`, data, {
    params: { teacherId: user.id }
  })
},

updateMaterial(id, data) {
  const user = getLocalUser()
  return apiClient.put(`/course/materials/${id}`, data, {
    params: { teacherId: user.id }
  })
},

deleteMaterial(id) {
  const user = getLocalUser()
  return apiClient.delete(`/course/materials/${id}`, {
    params: { teacherId: user.id }
  })
}
```

Add announcement methods:

```js
createAnnouncement(courseId, data) {
  const user = getLocalUser()
  return apiClient.post(`/course/${courseId}/announcements`, data, {
    params: { teacherId: user.id }
  })
},

updateAnnouncement(id, data) {
  const user = getLocalUser()
  return apiClient.put(`/course/announcements/${id}`, data, {
    params: { teacherId: user.id }
  })
},

deleteAnnouncement(id) {
  const user = getLocalUser()
  return apiClient.delete(`/course/announcements/${id}`, {
    params: { teacherId: user.id }
  })
}
```

- [ ] **Step 4: Run frontend build**

Run:

```bash
cd frontend
npm run build
```

Expected: build succeeds.

- [ ] **Step 5: Commit API methods**

Run:

```bash
git add frontend/src/api/index.js
git commit -m "feat: add course content frontend APIs"
```

Expected: commit contains only API wrapper changes.

---

## Task 7: VideoPlayer Component

**Files:**
- Create: `frontend/src/components/VideoPlayer.vue`

- [ ] **Step 1: Create component template**

Create a component with:

```vue
<template>
  <div class="video-player">
    <div class="video-stage">
      <video
        ref="videoRef"
        class="video-element"
        :src="src"
        :poster="poster"
        @loadedmetadata="handleLoadedMetadata"
        @timeupdate="handleTimeUpdate"
        @pause="emitPause"
        @ended="$emit('ended')"
        @error="handleError"
      />
      <div v-if="danmakuEnabled" class="danmaku-layer">
        <span
          v-for="item in activeDanmaku"
          :key="item.id + '-' + item.createdAt"
          class="danmaku-item"
          :style="{ color: item.color || '#ffffff', top: item.top + 'px' }"
        >
          {{ item.content }}
        </span>
      </div>
    </div>
    <div class="video-controls">
      <el-button size="small" @click="togglePlay">{{ playing ? '暂停' : '播放' }}</el-button>
      <span class="time-text">{{ formatTime(currentTime) }} / {{ formatTime(duration) }}</span>
      <input class="timeline" type="range" min="0" :max="duration || 0" step="1" v-model.number="currentTime" @input="seekToCurrent" />
      <el-select v-model="playbackRate" size="small" class="speed-select" @change="applyPlaybackRate">
        <el-option v-for="rate in speedOptions" :key="rate" :label="rate + 'x'" :value="rate" />
      </el-select>
      <el-button size="small" @click="toggleFullscreen">全屏</el-button>
    </div>
  </div>
</template>
```

- [ ] **Step 2: Add component script**

Add:

```js
<script>
export default {
  name: 'VideoPlayer',
  props: {
    src: { type: String, required: true },
    poster: { type: String, default: '' },
    initialTime: { type: Number, default: 0 },
    danmakuList: { type: Array, default: () => [] },
    danmakuEnabled: { type: Boolean, default: true }
  },
  emits: ['ready', 'timeupdate', 'pause', 'ended', 'progress-save', 'send-danmaku', 'error'],
  data() {
    return {
      playing: false,
      currentTime: 0,
      duration: 0,
      playbackRate: 1,
      speedOptions: [0.75, 1, 1.25, 1.5, 2],
      lastProgressEmitAt: 0
    }
  },
  computed: {
    activeDanmaku() {
      const now = Math.floor(this.currentTime)
      return this.danmakuList
        .filter(item => Math.abs((item.timeSeconds || 0) - now) <= 1)
        .slice(0, 6)
        .map((item, index) => ({ ...item, top: 16 + index * 28 }))
    }
  },
  watch: {
    src() {
      this.playing = false
      this.currentTime = 0
      this.$nextTick(() => {
        if (this.$refs.videoRef) this.$refs.videoRef.load()
      })
    }
  },
  beforeUnmount() {
    this.emitProgressSave()
  },
  methods: {
    handleLoadedMetadata() {
      const video = this.$refs.videoRef
      this.duration = Math.floor(video.duration || 0)
      if (this.initialTime > 0 && this.initialTime < this.duration) {
        video.currentTime = this.initialTime
        this.currentTime = this.initialTime
      }
      this.applyPlaybackRate()
      this.$emit('ready', { duration: this.duration })
    },
    handleTimeUpdate() {
      const video = this.$refs.videoRef
      this.currentTime = Math.floor(video.currentTime || 0)
      this.$emit('timeupdate', { currentTime: this.currentTime, duration: this.duration })
      if (Date.now() - this.lastProgressEmitAt > 15000) {
        this.emitProgressSave()
      }
    },
    emitPause() {
      this.playing = false
      this.emitProgressSave()
      this.$emit('pause', { currentTime: this.currentTime, duration: this.duration })
    },
    emitProgressSave() {
      this.lastProgressEmitAt = Date.now()
      this.$emit('progress-save', { currentTime: this.currentTime, duration: this.duration })
    },
    togglePlay() {
      const video = this.$refs.videoRef
      if (!video) return
      if (video.paused) {
        video.play()
        this.playing = true
      } else {
        video.pause()
        this.playing = false
      }
    },
    seekToCurrent() {
      const video = this.$refs.videoRef
      if (video) video.currentTime = this.currentTime
    },
    applyPlaybackRate() {
      const video = this.$refs.videoRef
      if (video) video.playbackRate = this.playbackRate
    },
    toggleFullscreen() {
      const root = this.$el
      if (root.requestFullscreen) root.requestFullscreen()
    },
    handleError() {
      this.playing = false
      this.$emit('error', '视频无法播放，请检查视频地址')
    },
    formatTime(seconds) {
      const value = Math.max(0, Math.floor(seconds || 0))
      const min = String(Math.floor(value / 60)).padStart(2, '0')
      const sec = String(value % 60).padStart(2, '0')
      return `${min}:${sec}`
    }
  }
}
</script>
```

- [ ] **Step 3: Add component styles**

Add scoped styles with stable dimensions:

```css
<style scoped>
.video-player { width: 100%; background: #111827; color: #fff; border-radius: 6px; overflow: hidden; }
.video-stage { position: relative; aspect-ratio: 16 / 9; background: #000; }
.video-element { width: 100%; height: 100%; display: block; object-fit: contain; }
.danmaku-layer { position: absolute; inset: 0; pointer-events: none; overflow: hidden; }
.danmaku-item { position: absolute; right: -20%; white-space: nowrap; text-shadow: 0 1px 3px rgba(0,0,0,.8); animation: danmaku-move 7s linear forwards; }
.video-controls { min-height: 48px; display: grid; grid-template-columns: auto auto 1fr 96px auto; gap: 10px; align-items: center; padding: 8px 10px; background: #1f2937; }
.time-text { min-width: 96px; font-size: 13px; color: #d1d5db; }
.timeline { width: 100%; }
.speed-select { width: 96px; }
@keyframes danmaku-move { from { transform: translateX(0); } to { transform: translateX(-140%); } }
@media (max-width: 700px) {
  .video-controls { grid-template-columns: auto 1fr auto; }
  .time-text { display: none; }
  .speed-select { width: 86px; }
}
</style>
```

- [ ] **Step 4: Run frontend build**

Run:

```bash
cd frontend
npm run build
```

Expected: build succeeds and `VideoPlayer.vue` compiles.

- [ ] **Step 5: Commit component**

Run:

```bash
git add frontend/src/components/VideoPlayer.vue
git commit -m "feat: add video player component"
```

Expected: commit contains only `VideoPlayer.vue`.

---

## Task 8: Student Learning Tab

**Files:**
- Modify: `frontend/src/views/CourseDetail.vue`

- [ ] **Step 1: Import and register `VideoPlayer`**

Change imports and components:

```js
import VideoPlayer from '../components/VideoPlayer.vue'

components: { StarRating, VideoPlayer },
```

- [ ] **Step 2: Add state**

Add to `data()`:

```js
learningContent: {
  chapters: [],
  videos: [],
  materials: [],
  announcements: []
},
selectedVideo: null,
selectedPlayInfo: null,
videoInitialTime: 0,
currentVideoTime: 0,
danmakuEnabled: true,
danmakuList: [],
danmakuInput: '',
progressSaving: false
```

- [ ] **Step 3: Add learning tab template**

Add a tab visible to students:

```vue
<el-tab-pane v-if="isStudent" label="课程学习" name="learning">
  <div class="learning-layout">
    <aside class="learning-sidebar">
      <div v-if="learningContent.announcements.length" class="announcement-strip">
        <strong>{{ learningContent.announcements[0].title }}</strong>
        <p>{{ learningContent.announcements[0].content }}</p>
      </div>
      <div v-for="chapter in chapterTree" :key="chapter.id" class="chapter-block">
        <h4>{{ chapter.title }}</h4>
        <button
          v-for="video in chapter.videos"
          :key="video.id"
          class="lesson-row"
          @click="selectVideo(video)"
        >
          {{ video.title }}
        </button>
        <div v-for="child in chapter.children" :key="child.id" class="child-chapter">
          <h5>{{ child.title }}</h5>
          <button
            v-for="video in child.videos"
            :key="video.id"
            class="lesson-row"
            @click="selectVideo(video)"
          >
            {{ video.title }}
          </button>
          <a
            v-for="material in child.materials"
            :key="material.id"
            class="material-row"
            :href="material.resourceUrl"
            target="_blank"
            rel="noreferrer"
          >
            {{ material.title }}
          </a>
        </div>
        <a
          v-for="material in chapter.materials"
          :key="material.id"
          class="material-row"
          :href="material.resourceUrl"
          target="_blank"
          rel="noreferrer"
        >
          {{ material.title }}
        </a>
      </div>
    </aside>
    <section class="learning-player">
      <VideoPlayer
        v-if="selectedPlayInfo"
        :src="selectedPlayInfo.videoUrl"
        :initial-time="videoInitialTime"
        :danmaku-list="danmakuList"
        :danmaku-enabled="danmakuEnabled"
        @timeupdate="handleVideoTimeUpdate"
        @progress-save="handleProgressSave"
        @error="handleVideoError"
      />
      <el-empty v-else description="请选择视频开始学习" />
      <div v-if="selectedPlayInfo" class="danmaku-panel">
        <el-switch v-model="danmakuEnabled" active-text="弹幕" />
        <el-input v-model="danmakuInput" maxlength="200" aria-label="发送弹幕" />
        <el-button type="primary" @click="sendDanmaku">发送</el-button>
      </div>
    </section>
  </div>
</el-tab-pane>
```

- [ ] **Step 4: Add computed chapter tree**

Add:

```js
chapterTree() {
  return this.learningContent.chapters
    .filter(chapter => !chapter.parentId)
    .map(chapter => ({
      ...chapter,
      children: this.learningContent.chapters
        .filter(child => child.parentId === chapter.id)
        .map(child => ({
          ...child,
          videos: this.learningContent.videos.filter(video => video.chapterId === child.id && video.status === 'published'),
          materials: this.learningContent.materials.filter(material => material.chapterId === child.id)
        })),
      videos: this.learningContent.videos.filter(video => video.chapterId === chapter.id && video.status === 'published'),
      materials: this.learningContent.materials.filter(material => material.chapterId === chapter.id)
    }))
}
```

- [ ] **Step 5: Add learning methods**

Add:

```js
async loadLearningContent() {
  try {
    const result = await api.getLearningContent(this.courseId)
    this.learningContent = result.data || { chapters: [], videos: [], materials: [], announcements: [] }
  } catch (error) {
    ElMessage.error('加载课程学习内容失败')
  }
},

async selectVideo(video) {
  try {
    this.selectedVideo = video
    this.currentVideoTime = 0
    const playRes = await api.getVideoPlayInfo(video.id)
    this.selectedPlayInfo = playRes.data
    const progressRes = await api.getVideoProgress(video.id)
    this.videoInitialTime = progressRes.data?.lastPosition || 0
    this.currentVideoTime = this.videoInitialTime
    const danmakuRes = await api.getVideoDanmaku(video.id)
    this.danmakuList = danmakuRes.data || []
  } catch (error) {
    ElMessage.error('加载视频失败')
  }
},

handleVideoTimeUpdate(payload) {
  this.currentVideoTime = payload.currentTime || 0
},

async handleProgressSave(payload) {
  if (!this.selectedVideo || this.progressSaving) return
  this.progressSaving = true
  try {
    await api.saveVideoProgress(this.selectedVideo.id, payload.currentTime, payload.duration)
  } catch (error) {
    console.warn('保存播放进度失败:', error)
  } finally {
    this.progressSaving = false
  }
},

async sendDanmaku() {
  if (!this.selectedVideo || !this.danmakuInput.trim()) return
  try {
    await api.sendVideoDanmaku(this.selectedVideo.id, {
      content: this.danmakuInput.trim(),
      timeSeconds: this.currentVideoTime,
      color: '#ffffff'
    })
    this.danmakuInput = ''
    const result = await api.getVideoDanmaku(this.selectedVideo.id)
    this.danmakuList = result.data || []
  } catch (error) {
    ElMessage.error('发送弹幕失败')
  }
},

handleVideoError(message) {
  ElMessage.error(message)
}
```

- [ ] **Step 6: Load learning content when mounted**

In `loadCourseDetail()` after role-specific content:

```js
if (this.isStudent || this.isTeacher) this.loadLearningContent()
```

- [ ] **Step 7: Add layout styles**

Add scoped CSS:

```css
.learning-layout { display: grid; grid-template-columns: 280px 1fr; gap: 16px; }
.learning-sidebar { background: #fff; border: 1px solid #ebeef5; border-radius: 6px; padding: 12px; max-height: 620px; overflow: auto; }
.announcement-strip { border-left: 3px solid #409EFF; padding-left: 10px; margin-bottom: 14px; color: #303133; }
.announcement-strip p { margin: 6px 0 0; color: #606266; font-size: 13px; line-height: 1.5; }
.chapter-block { margin-bottom: 14px; }
.chapter-block h4 { margin: 0 0 8px; color: #303133; }
.lesson-row, .material-row { display: block; width: 100%; text-align: left; padding: 8px 10px; margin-bottom: 6px; border: 1px solid #e4e7ed; border-radius: 4px; background: #f9fafb; color: #303133; text-decoration: none; cursor: pointer; }
.child-chapter { margin-left: 12px; padding-left: 10px; border-left: 2px solid #ebeef5; }
.child-chapter h5 { margin: 8px 0 6px; color: #606266; }
.learning-player { min-width: 0; }
.danmaku-panel { display: grid; grid-template-columns: auto 1fr auto; gap: 10px; align-items: center; margin-top: 12px; }
@media (max-width: 900px) {
  .learning-layout { grid-template-columns: 1fr; }
  .learning-sidebar { max-height: none; }
}
```

- [ ] **Step 8: Run frontend build**

Run:

```bash
cd frontend
npm run build
```

Expected: build succeeds.

- [ ] **Step 9: Commit student learning UI**

Run:

```bash
git add frontend/src/views/CourseDetail.vue
git commit -m "feat: add student course learning tab"
```

Expected: commit contains `CourseDetail.vue` learning changes.

---

## Task 9: Teacher Course and Content Management UI

**Files:**
- Modify: `frontend/src/views/TeacherDashboard.vue`
- Modify: `frontend/src/views/CourseDetail.vue`

- [ ] **Step 1: Add create course entry to `TeacherDashboard.vue`**

Add menu item:

```vue
<el-menu-item index="course-management">课程管理</el-menu-item>
```

Add panel:

```vue
<div v-if="activeMenu === 'course-management'">
  <div class="toolbar-row">
    <h3>课程管理</h3>
    <el-button type="primary" @click="showCourseDialog()">创建课程</el-button>
  </div>
  <el-table :data="myCourses" border>
    <el-table-column prop="name" label="课程名称" />
    <el-table-column prop="code" label="课程代码" />
    <el-table-column prop="semester" label="学期" />
    <el-table-column prop="status" label="状态" />
    <el-table-column label="操作">
      <template #default="scope">
        <el-button size="small" @click="showCourseDialog(scope.row)">编辑</el-button>
        <el-button type="primary" size="small" @click="viewCourseDetail(scope.row.id)">内容管理</el-button>
      </template>
    </el-table-column>
  </el-table>
</div>
```

- [ ] **Step 2: Add course dialog data and methods**

Add data:

```js
courseDialogVisible: false,
editingCourseId: null,
courseForm: {
  name: '',
  code: '',
  teacherId: null,
  teacherName: '',
  description: '',
  credit: 3,
  maxStudents: 50,
  category: '',
  department: '',
  coverImage: '',
  syllabus: '',
  semester: '',
  status: 'open'
}
```

Add methods:

```js
showCourseDialog(course) {
  this.editingCourseId = course?.id || null
  this.courseForm = course ? { ...course } : {
    name: '',
    code: '',
    teacherId: this.userInfo.id,
    teacherName: this.userInfo.name || this.userInfo.username,
    description: '',
    credit: 3,
    maxStudents: 50,
    category: '',
    department: this.userInfo.department || '',
    coverImage: '',
    syllabus: '',
    semester: '',
    status: 'open'
  }
  this.courseDialogVisible = true
},

async saveCourse() {
  try {
    if (this.editingCourseId) {
      await api.updateCourse(this.editingCourseId, this.courseForm)
    } else {
      await api.createCourse(this.courseForm)
    }
    ElMessage.success('保存成功')
    this.courseDialogVisible = false
    this.loadMyCourses()
  } catch (error) {
    ElMessage.error('保存课程失败')
  }
}
```

Add `api.createCourse(data)` to `frontend/src/api/index.js` if missing:

```js
createCourse(data) {
  return apiClient.post('/course/create', data)
}
```

- [ ] **Step 3: Add teacher content management tab in `CourseDetail.vue`**

Add a teacher-only tab:

```vue
<el-tab-pane v-if="isTeacher" label="内容管理" name="content-manage">
  <div class="manage-grid">
    <el-card>
      <template #header>章节</template>
      <el-button type="primary" size="small" @click="showChapterDialog()">新增章节</el-button>
      <el-table :data="learningContent.chapters" border>
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="sortOrder" label="排序" width="90" />
        <el-table-column label="操作" width="160">
          <template #default="scope">
            <el-button size="small" @click="showChapterDialog(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="deleteChapter(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <el-card>
      <template #header>视频</template>
      <el-button type="primary" size="small" @click="showVideoDialog()">新增视频</el-button>
      <el-table :data="learningContent.videos" border>
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column label="操作" width="160">
          <template #default="scope">
            <el-button size="small" @click="showVideoDialog(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="deleteVideo(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</el-tab-pane>
```

Add material and announcement cards:

```vue
<el-card>
  <template #header>课件</template>
  <el-button type="primary" size="small" @click="showMaterialDialog()">新增课件</el-button>
  <el-table :data="learningContent.materials" border>
    <el-table-column prop="title" label="标题" />
    <el-table-column prop="fileType" label="类型" width="100" />
    <el-table-column prop="resourceUrl" label="地址" />
    <el-table-column label="操作" width="160">
      <template #default="scope">
        <el-button size="small" @click="showMaterialDialog(scope.row)">编辑</el-button>
        <el-button type="danger" size="small" @click="deleteMaterial(scope.row.id)">删除</el-button>
      </template>
    </el-table-column>
  </el-table>
</el-card>

<el-card>
  <template #header>公告</template>
  <el-button type="primary" size="small" @click="showAnnouncementDialog()">发布公告</el-button>
  <el-table :data="learningContent.announcements" border>
    <el-table-column prop="title" label="标题" />
    <el-table-column prop="publishedAt" label="发布时间" width="180" />
    <el-table-column label="操作" width="160">
      <template #default="scope">
        <el-button size="small" @click="showAnnouncementDialog(scope.row)">编辑</el-button>
        <el-button type="danger" size="small" @click="deleteAnnouncement(scope.row.id)">删除</el-button>
      </template>
    </el-table-column>
  </el-table>
</el-card>
```

- [ ] **Step 4: Add management dialog methods**

Implement these methods in `CourseDetail.vue`:

```js
showChapterDialog(chapter) { this.editingChapter = chapter ? { ...chapter } : { title: '', parentId: null, sortOrder: 0 }; this.chapterDialogVisible = true },
saveChapter() { return this.editingChapter.id ? api.updateChapter(this.editingChapter.id, this.editingChapter) : api.createChapter(this.courseId, this.editingChapter) },
deleteChapter(id) { return api.deleteChapter(id).then(() => this.loadLearningContent()) },

showVideoDialog(video) { this.editingVideo = video ? { ...video } : { title: '', description: '', chapterId: null, videoUrl: '', duration: 0, sortOrder: 0, status: 'draft' }; this.videoDialogVisible = true },
saveVideo() { return this.editingVideo.id ? api.updateVideo(this.editingVideo.id, this.editingVideo) : api.createVideo(this.courseId, this.editingVideo) },
deleteVideo(id) { return api.deleteVideo(id).then(() => this.loadLearningContent()) },

showMaterialDialog(material) { this.editingMaterial = material ? { ...material } : { title: '', fileType: '', resourceUrl: '', chapterId: null, sortOrder: 0 }; this.materialDialogVisible = true },
saveMaterial() { return this.editingMaterial.id ? api.updateMaterial(this.editingMaterial.id, this.editingMaterial) : api.createMaterial(this.courseId, this.editingMaterial) },
deleteMaterial(id) { return api.deleteMaterial(id).then(() => this.loadLearningContent()) },

showAnnouncementDialog(announcement) { this.editingAnnouncement = announcement ? { ...announcement } : { title: '', content: '' }; this.announcementDialogVisible = true },
saveAnnouncement() { return this.editingAnnouncement.id ? api.updateAnnouncement(this.editingAnnouncement.id, this.editingAnnouncement) : api.createAnnouncement(this.courseId, this.editingAnnouncement) },
deleteAnnouncement(id) { return api.deleteAnnouncement(id).then(() => this.loadLearningContent()) }
```

After each save, close the dialog, show `ElMessage.success('保存成功')`, and call `loadLearningContent()`.

- [ ] **Step 5: Run frontend build**

Run:

```bash
cd frontend
npm run build
```

Expected: build succeeds.

- [ ] **Step 6: Commit teacher management UI**

Run:

```bash
git add frontend/src/views/TeacherDashboard.vue frontend/src/views/CourseDetail.vue frontend/src/api/index.js
git commit -m "feat: add teacher course content management"
```

Expected: commit contains teacher management UI and any API method needed by it.

---

## Task 10: End-to-End Verification

**Files:**
- No source files required unless verification reveals compile errors.

- [ ] **Step 1: Run backend build**

Run:

```bash
mvn -pl common,course-service -am package -DskipTests
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 2: Run frontend build**

Run:

```bash
cd frontend
npm run build
```

Expected: Vite production build succeeds.

- [ ] **Step 3: Manual API smoke checks**

With backend services running and logged-in IDs from seeded data:

```bash
curl "http://localhost:8080/api/course/1/learning-content?userId=5&role=student"
curl "http://localhost:8080/api/course/videos/1/play-info?userId=5&role=student"
```

Expected:

- The first call returns `code: 200` with `chapters`, `videos`, `materials`, and `announcements`.
- The second call returns `code: 200` only after a video record exists and the student selected the course.

- [ ] **Step 4: Manual browser checks**

Run:

```bash
cd frontend
npm run dev
```

Open `http://localhost:3000`.

Check:

- Teacher logs in and creates a course.
- Teacher opens course detail and creates a chapter.
- Teacher adds a video URL such as a known browser-playable MP4 URL.
- Student selected into the course opens the learning tab.
- Student plays the video, changes playback speed, pauses, returns, and resumes near the saved time.
- Student toggles danmaku and sends a shared danmaku.

- [ ] **Step 5: Final commit for verification fixes**

If verification required source fixes, commit only those files:

```bash
git add <changed-files>
git commit -m "fix: stabilize course video learning flow"
```

Expected: no unrelated files included.

---

## Implementation Notes

- Do not use Aliyun VOD in this phase.
- Do not add local video upload.
- Do not add server-side transcoding.
- Do not use prohibited recursive delete commands.
- Delete only explicit single records through repository methods or explicit single-file shell commands.
- Preserve existing project patterns, including `Result<T>` response wrapping and frontend `api/index.js` response handling.
- The project currently has no test framework; use backend package builds, frontend build, and manual checks as verification gates.

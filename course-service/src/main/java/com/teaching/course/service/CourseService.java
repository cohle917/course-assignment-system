package com.teaching.course.service;

import com.teaching.common.entity.*;
import com.teaching.course.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseSelectionRepository courseSelectionRepository;
    private final CourseCommentRepository courseCommentRepository;
    private final CourseChapterRepository courseChapterRepository;
    private final CourseVideoRepository courseVideoRepository;
    private final CourseMaterialRepository courseMaterialRepository;
    private final CourseAnnouncementRepository courseAnnouncementRepository;
    private final VideoProgressRepository videoProgressRepository;
    private final VideoDanmakuRepository videoDanmakuRepository;

    public CourseService(CourseRepository courseRepository,
                         CourseSelectionRepository courseSelectionRepository,
                         CourseCommentRepository courseCommentRepository,
                         CourseChapterRepository courseChapterRepository,
                         CourseVideoRepository courseVideoRepository,
                         CourseMaterialRepository courseMaterialRepository,
                         CourseAnnouncementRepository courseAnnouncementRepository,
                         VideoProgressRepository videoProgressRepository,
                         VideoDanmakuRepository videoDanmakuRepository) {
        this.courseRepository = courseRepository;
        this.courseSelectionRepository = courseSelectionRepository;
        this.courseCommentRepository = courseCommentRepository;
        this.courseChapterRepository = courseChapterRepository;
        this.courseVideoRepository = courseVideoRepository;
        this.courseMaterialRepository = courseMaterialRepository;
        this.courseAnnouncementRepository = courseAnnouncementRepository;
        this.videoProgressRepository = videoProgressRepository;
        this.videoDanmakuRepository = videoDanmakuRepository;
    }

    @PersistenceContext
    private EntityManager entityManager;

    // ==================== 课程查询 ====================

    /**
     * 组合筛选 + 排序获取课程列表
     */
    public List<Course> getCourses(String keyword, String category, String department,
                                   String semester, String sortBy) {
        List<Course> courses;

        // Step 1: 基础查询
        if (keyword != null && !keyword.trim().isEmpty()) {
            courses = courseRepository.findByNameContainingOrDescriptionContaining(keyword, keyword);
        } else {
            courses = courseRepository.findAll();
        }

        // Step 2: 内存筛选（叠加 AND 条件）
        if (category != null && !category.trim().isEmpty()) {
            courses = courses.stream()
                    .filter(c -> category.equals(c.getCategory()))
                    .collect(Collectors.toList());
        }
        if (department != null && !department.trim().isEmpty()) {
            courses = courses.stream()
                    .filter(c -> department.equals(c.getDepartment()))
                    .collect(Collectors.toList());
        }
        if (semester != null && !semester.trim().isEmpty()) {
            courses = courses.stream()
                    .filter(c -> semester.equals(c.getSemester()))
                    .collect(Collectors.toList());
        }

        // Step 3: 排序
        if ("popularity".equals(sortBy)) {
            courses.sort((a, b) -> Integer.compare(
                    b.getPopularity() != null ? b.getPopularity() : 0,
                    a.getPopularity() != null ? a.getPopularity() : 0));
        } else if ("newest".equals(sortBy)) {
            courses.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        }
        // "default" — no sort, keep DB order

        return courses;
    }

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public List<Course> getCoursesByTeacher(Long teacherId) {
        return courseRepository.findByTeacherId(teacherId);
    }

    public List<Course> getCoursesByStudent(Long studentId) {
        List<CourseSelection> selections = courseSelectionRepository.findByStudentId(studentId);
        List<Long> courseIds = selections.stream()
                .map(CourseSelection::getCourseId)
                .collect(Collectors.toList());
        return courseRepository.findAllById(courseIds);
    }

    /**
     * 获取单课程详情，附带讲师信息
     */
    public Map<String, Object> getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("课程不存在"));

        Map<String, Object> result = new HashMap<>();
        result.put("id", course.getId());
        result.put("name", course.getName());
        result.put("code", course.getCode());
        result.put("teacherId", course.getTeacherId());
        result.put("teacherName", course.getTeacherName());
        result.put("description", course.getDescription());
        result.put("credit", course.getCredit());
        result.put("maxStudents", course.getMaxStudents());
        result.put("currentStudents", course.getCurrentStudents());
        result.put("semester", course.getSemester());
        result.put("status", course.getStatus() != null ? course.getStatus().name() : null);
        result.put("category", course.getCategory());
        result.put("department", course.getDepartment());
        result.put("coverImage", course.getCoverImage());
        result.put("syllabus", course.getSyllabus());
        result.put("popularity", course.getPopularity());
        result.put("createdAt", course.getCreatedAt());

        // 查询讲师信息（JOIN users 表）
        Object[] teacherData = (Object[]) entityManager
                .createNativeQuery("SELECT id, username, name, email, phone, department " +
                        "FROM users WHERE id = ?")
                .setParameter(1, course.getTeacherId())
                .getSingleResult();

        Map<String, Object> teacherInfo = new HashMap<>();
        teacherInfo.put("id", teacherData[0]);
        teacherInfo.put("username", teacherData[1]);
        teacherInfo.put("name", teacherData[2]);
        teacherInfo.put("email", teacherData[3]);
        teacherInfo.put("phone", teacherData[4]);
        teacherInfo.put("department", teacherData[5]);
        result.put("teacherInfo", teacherInfo);

        // 平均评分
        Double avgRating = courseCommentRepository.findAverageRatingByCourseId(id);
        result.put("avgRating", avgRating != null ? Math.round(avgRating * 10) / 10.0 : 0);

        // 评价数量
        long reviewCount = courseCommentRepository.countByCourseIdAndParentIdIsNull(id);
        result.put("reviewCount", reviewCount);

        return result;
    }

    // ==================== 筛选项查询 ====================

    public List<String> getCategories() {
        return courseRepository.findDistinctCategories();
    }

    public List<String> getDepartments() {
        return courseRepository.findDistinctDepartments();
    }

    public List<String> getSemesters() {
        return courseRepository.findDistinctSemesters();
    }

    // ==================== 选课管理 ====================

    @Transactional
    public void selectCourse(Long courseId, Long studentId) {
        if (courseSelectionRepository.existsByCourseIdAndStudentId(courseId, studentId)) {
            throw new RuntimeException("已经选过该课程");
        }

        CourseSelection selection = new CourseSelection();
        selection.setCourseId(courseId);
        selection.setStudentId(studentId);
        courseSelectionRepository.save(selection);

        // 更新热度值
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course != null) {
            course.setCurrentStudents((course.getCurrentStudents() != null ? course.getCurrentStudents() : 0) + 1);
            course.setPopularity((course.getPopularity() != null ? course.getPopularity() : 0) + 10);
            courseRepository.save(course);
        }
    }

    @Transactional
    public void dropCourse(Long courseId, Long studentId) {
        CourseSelection selection = courseSelectionRepository
                .findByCourseIdAndStudentId(courseId, studentId)
                .orElseThrow(() -> new RuntimeException("未选该课程"));

        courseSelectionRepository.delete(selection);

        // 更新热度值
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course != null) {
            course.setCurrentStudents(Math.max(0,
                    (course.getCurrentStudents() != null ? course.getCurrentStudents() : 1) - 1));
            course.setPopularity(Math.max(0,
                    (course.getPopularity() != null ? course.getPopularity() : 10) - 10));
            courseRepository.save(course);
        }
    }

    public List<Map<String, Object>> getCourseStudents(Long courseId) {
        // 修复：JOIN users 表获取真实学生数据
        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createNativeQuery(
                "SELECT u.id, u.username, u.name, u.email, cs.grade " +
                "FROM course_selections cs " +
                "JOIN users u ON cs.student_id = u.id " +
                "WHERE cs.course_id = ?")
                .setParameter(1, courseId)
                .getResultList();

        return rows.stream().map(row -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", row[0]);
            map.put("username", row[1]);
            map.put("name", row[2]);
            map.put("email", row[3]);
            map.put("grade", row[4]);
            return map;
        }).collect(Collectors.toList());
    }

    public Course createCourse(Course course) {
        if (course.getPopularity() == null) course.setPopularity(0);
        if (course.getCurrentStudents() == null) course.setCurrentStudents(0);
        return courseRepository.save(course);
    }

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
        if (input.getStatus() != null) {
            course.setStatus(input.getStatus());
        }
        return courseRepository.save(course);
    }

    public void deleteCourse(Long id) {
        courseRepository.deleteById(id);
    }

    // ==================== 璇剧▼鍐呭绠＄悊 ====================

    public Map<String, Object> getLearningContent(Long courseId, Long userId, String role) {
        requireCourseAccess(courseId, userId, role);
        Map<String, Object> result = new HashMap<>();
        result.put("chapters", courseChapterRepository.findByCourseIdOrderBySortOrderAscIdAsc(courseId));
        result.put("videos", courseVideoRepository.findByCourseIdOrderBySortOrderAscIdAsc(courseId));
        result.put("materials", courseMaterialRepository.findByCourseIdOrderBySortOrderAscIdAsc(courseId));
        result.put("announcements", courseAnnouncementRepository.findByCourseIdOrderByPublishedAtDesc(courseId));
        return result;
    }

    @Transactional
    public CourseChapter saveChapter(Long courseId, Long teacherId, CourseChapter chapter) {
        requireTeacherOwnsCourse(courseId, teacherId);
        ensureChapterParentBelongsToCourse(courseId, chapter.getParentId());
        chapter.setCourseId(courseId);
        if (chapter.getSortOrder() == null) {
            chapter.setSortOrder(0);
        }
        return courseChapterRepository.save(chapter);
    }

    @Transactional
    public CourseChapter updateChapter(Long chapterId, Long teacherId, CourseChapter input) {
        CourseChapter chapter = courseChapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("Chapter does not exist"));
        requireTeacherOwnsCourse(chapter.getCourseId(), teacherId);
        ensureChapterParentBelongsToCourse(chapter.getCourseId(), input.getParentId());
        chapter.setParentId(input.getParentId());
        chapter.setTitle(input.getTitle());
        chapter.setSortOrder(input.getSortOrder() != null ? input.getSortOrder() : 0);
        return courseChapterRepository.save(chapter);
    }

    @Transactional
    public void deleteChapter(Long chapterId, Long teacherId) {
        CourseChapter chapter = courseChapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("Chapter does not exist"));
        requireTeacherOwnsCourse(chapter.getCourseId(), teacherId);
        courseChapterRepository.deleteById(chapterId);
    }

    @Transactional
    public CourseVideo saveVideo(Long courseId, Long teacherId, CourseVideo video) {
        requireTeacherOwnsCourse(courseId, teacherId);
        validateHttpUrl(video.getVideoUrl(), "Video URL");
        ensureChapterBelongsToCourse(courseId, video.getChapterId());
        video.setCourseId(courseId);
        video.setVideoUrl(video.getVideoUrl().trim());
        if (video.getStatus() == null) {
            video.setStatus(CourseVideo.VideoStatus.draft);
        }
        if (video.getSortOrder() == null) {
            video.setSortOrder(0);
        }
        if (video.getDuration() == null) {
            video.setDuration(0);
        }
        return courseVideoRepository.save(video);
    }

    @Transactional
    public CourseVideo updateVideo(Long videoId, Long teacherId, CourseVideo input) {
        CourseVideo video = requireVideo(videoId);
        requireTeacherOwnsCourse(video.getCourseId(), teacherId);
        if (input.getVideoUrl() != null) {
            validateHttpUrl(input.getVideoUrl(), "Video URL");
            video.setVideoUrl(input.getVideoUrl().trim());
        }
        ensureChapterBelongsToCourse(video.getCourseId(), input.getChapterId());
        video.setChapterId(input.getChapterId());
        video.setTitle(input.getTitle());
        video.setDescription(input.getDescription());
        video.setDuration(input.getDuration() != null ? input.getDuration() : 0);
        video.setSortOrder(input.getSortOrder() != null ? input.getSortOrder() : 0);
        if (input.getStatus() != null) {
            video.setStatus(input.getStatus());
        }
        return courseVideoRepository.save(video);
    }

    @Transactional
    public void deleteVideo(Long videoId, Long teacherId) {
        CourseVideo video = requireVideo(videoId);
        requireTeacherOwnsCourse(video.getCourseId(), teacherId);
        courseVideoRepository.deleteById(videoId);
    }

    @Transactional
    public CourseMaterial saveMaterial(Long courseId, Long teacherId, CourseMaterial material) {
        requireTeacherOwnsCourse(courseId, teacherId);
        validateHttpUrl(material.getResourceUrl(), "Material URL");
        ensureChapterBelongsToCourse(courseId, material.getChapterId());
        material.setCourseId(courseId);
        material.setResourceUrl(material.getResourceUrl().trim());
        if (material.getSortOrder() == null) {
            material.setSortOrder(0);
        }
        return courseMaterialRepository.save(material);
    }

    @Transactional
    public CourseMaterial updateMaterial(Long materialId, Long teacherId, CourseMaterial input) {
        CourseMaterial material = courseMaterialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("Material does not exist"));
        requireTeacherOwnsCourse(material.getCourseId(), teacherId);
        if (input.getResourceUrl() != null) {
            validateHttpUrl(input.getResourceUrl(), "Material URL");
            material.setResourceUrl(input.getResourceUrl().trim());
        }
        ensureChapterBelongsToCourse(material.getCourseId(), input.getChapterId());
        material.setChapterId(input.getChapterId());
        material.setTitle(input.getTitle());
        material.setFileType(input.getFileType());
        material.setSortOrder(input.getSortOrder() != null ? input.getSortOrder() : 0);
        return courseMaterialRepository.save(material);
    }

    @Transactional
    public void deleteMaterial(Long materialId, Long teacherId) {
        CourseMaterial material = courseMaterialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("Material does not exist"));
        requireTeacherOwnsCourse(material.getCourseId(), teacherId);
        courseMaterialRepository.deleteById(materialId);
    }

    @Transactional
    public CourseAnnouncement saveAnnouncement(Long courseId, Long teacherId, CourseAnnouncement announcement) {
        requireTeacherOwnsCourse(courseId, teacherId);
        announcement.setCourseId(courseId);
        if (announcement.getPublishedAt() == null) {
            announcement.setPublishedAt(LocalDateTime.now());
        }
        return courseAnnouncementRepository.save(announcement);
    }

    @Transactional
    public CourseAnnouncement updateAnnouncement(Long announcementId, Long teacherId, CourseAnnouncement input) {
        CourseAnnouncement announcement = courseAnnouncementRepository.findById(announcementId)
                .orElseThrow(() -> new RuntimeException("Announcement does not exist"));
        requireTeacherOwnsCourse(announcement.getCourseId(), teacherId);
        announcement.setTitle(input.getTitle());
        announcement.setContent(input.getContent());
        if (input.getPublishedAt() != null) {
            announcement.setPublishedAt(input.getPublishedAt());
        }
        return courseAnnouncementRepository.save(announcement);
    }

    @Transactional
    public void deleteAnnouncement(Long announcementId, Long teacherId) {
        CourseAnnouncement announcement = courseAnnouncementRepository.findById(announcementId)
                .orElseThrow(() -> new RuntimeException("Announcement does not exist"));
        requireTeacherOwnsCourse(announcement.getCourseId(), teacherId);
        courseAnnouncementRepository.deleteById(announcementId);
    }

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
        result.put("status", video.getStatus());
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
        if (content.isEmpty()) {
            throw new RuntimeException("Danmaku content cannot be empty");
        }
        if (content.length() > 200) {
            throw new RuntimeException("Danmaku content cannot exceed 200 characters");
        }
        danmaku.setCourseId(video.getCourseId());
        danmaku.setVideoId(videoId);
        danmaku.setContent(content);
        if (danmaku.getTimeSeconds() == null || danmaku.getTimeSeconds() < 0) {
            danmaku.setTimeSeconds(0);
        }
        if (danmaku.getColor() == null || danmaku.getColor().trim().isEmpty()) {
            danmaku.setColor("#ffffff");
        }
        if (danmaku.getStatus() == null) {
            danmaku.setStatus(VideoDanmaku.DanmakuStatus.visible);
        }
        return videoDanmakuRepository.save(danmaku);
    }

    // ==================== 评论管理 ====================

    public List<Map<String, Object>> getCourseComments(Long courseId) {
        List<CourseComment> allComments = courseCommentRepository.findByCourseIdOrderByCreatedAtDesc(courseId);

        List<Map<String, Object>> rootComments = new ArrayList<>();
        Map<Long, List<Map<String, Object>>> replyMap = new HashMap<>();

        for (CourseComment comment : allComments) {
            Map<String, Object> commentMap = new HashMap<>();
            commentMap.put("id", comment.getId());
            commentMap.put("courseId", comment.getCourseId());
            commentMap.put("userId", comment.getUserId());
            commentMap.put("username", comment.getUsername());
            commentMap.put("userName", comment.getUserName());
            commentMap.put("userRole", comment.getUserRole());
            commentMap.put("parentId", comment.getParentId());
            commentMap.put("content", comment.getContent());
            commentMap.put("rating", comment.getRating());
            commentMap.put("createdAt", comment.getCreatedAt());
            commentMap.put("replyCount", courseCommentRepository.countByParentId(comment.getId()));

            if (comment.getParentId() == null) {
                rootComments.add(commentMap);
            } else {
                replyMap.computeIfAbsent(comment.getParentId(), k -> new ArrayList<>()).add(commentMap);
            }
        }

        for (Map<String, Object> rootComment : rootComments) {
            Long rootId = (Long) rootComment.get("id");
            rootComment.put("replies", replyMap.getOrDefault(rootId, new ArrayList<>()));
        }

        return rootComments;
    }

    @Transactional
    public CourseComment addComment(CourseComment comment) {
        CourseComment saved = courseCommentRepository.save(comment);

        // 如果有评分，更新课程热度
        if (comment.getRating() != null && comment.getParentId() == null) {
            Course course = courseRepository.findById(comment.getCourseId()).orElse(null);
            if (course != null) {
                course.setPopularity((course.getPopularity() != null ? course.getPopularity() : 0) + 5);
                courseRepository.save(course);
            }
        }

        return saved;
    }

    public void deleteComment(Long commentId) {
        deleteCommentWithReplies(commentId);
    }

    private Course requireCourse(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course does not exist"));
    }

    private CourseVideo requireVideo(Long videoId) {
        return courseVideoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video does not exist"));
    }

    private void requireTeacherOwnsCourse(Long courseId, Long teacherId) {
        Course course = requireCourse(courseId);
        if (teacherId == null || !teacherId.equals(course.getTeacherId())) {
            throw new RuntimeException("No permission to manage this course");
        }
    }

    private void requireStudentSelectedCourse(Long courseId, Long studentId) {
        if (studentId == null || !courseSelectionRepository.existsByCourseIdAndStudentId(courseId, studentId)) {
            throw new RuntimeException("Student has not selected this course");
        }
    }

    private void requireCourseAccess(Long courseId, Long userId, String role) {
        Course course = requireCourse(courseId);
        if ("teacher".equalsIgnoreCase(role) && userId != null && userId.equals(course.getTeacherId())) {
            return;
        }
        if ("student".equalsIgnoreCase(role)) {
            requireStudentSelectedCourse(courseId, userId);
            return;
        }
        throw new RuntimeException("No permission to access this course content");
    }

    private void validateHttpUrl(String url, String fieldName) {
        if (url == null || url.trim().isEmpty()) {
            throw new RuntimeException(fieldName + " cannot be empty");
        }
        String normalized = url.trim().toLowerCase();
        if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
            throw new RuntimeException(fieldName + " must be an http or https URL");
        }
    }

    private void ensureChapterBelongsToCourse(Long courseId, Long chapterId) {
        if (chapterId == null) {
            return;
        }
        CourseChapter chapter = courseChapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("Chapter does not exist"));
        if (!courseId.equals(chapter.getCourseId())) {
            throw new RuntimeException("Chapter does not belong to this course");
        }
    }

    private void ensureChapterParentBelongsToCourse(Long courseId, Long parentId) {
        ensureChapterBelongsToCourse(courseId, parentId);
    }

    private void deleteCommentWithReplies(Long parentId) {
        List<CourseComment> replies = courseCommentRepository.findByParentIdOrderByCreatedAtAsc(parentId);
        for (CourseComment reply : replies) {
            deleteCommentWithReplies(reply.getId());
        }
        courseCommentRepository.deleteById(parentId);
    }
}

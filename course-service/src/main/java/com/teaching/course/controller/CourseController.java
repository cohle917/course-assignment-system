package com.teaching.course.controller;

import com.teaching.common.entity.*;
import com.teaching.common.result.Result;
import com.teaching.course.service.CourseService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/course")
@CrossOrigin(origins = "*")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    // ==================== 课程查询 ====================

    @GetMapping("/list")
    public Result<List<Course>> getCourses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false, defaultValue = "default") String sortBy) {
        try {
            List<Course> courses = courseService.getCourses(keyword, category, department, semester, sortBy);
            return Result.success(courses);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> getCourseById(@PathVariable Long id) {
        try {
            Map<String, Object> course = courseService.getCourseById(id);
            return Result.success(course);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/my-courses")
    public Result<List<Course>> getMyCourses(@RequestParam Long studentId) {
        try {
            List<Course> courses = courseService.getCoursesByStudent(studentId);
            return Result.success(courses);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/teacher-courses")
    public Result<List<Course>> getTeacherCourses(@RequestParam Long teacherId) {
        try {
            List<Course> courses = courseService.getCoursesByTeacher(teacherId);
            return Result.success(courses);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ==================== 筛选项 ====================

    @GetMapping("/categories")
    public Result<List<String>> getCategories() {
        try {
            return Result.success(courseService.getCategories());
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/departments")
    public Result<List<String>> getDepartments() {
        try {
            return Result.success(courseService.getDepartments());
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/semesters")
    public Result<List<String>> getSemesters() {
        try {
            return Result.success(courseService.getSemesters());
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ==================== 选课管理 ====================

    @PostMapping("/select")
    public Result<Void> selectCourse(@RequestBody Map<String, Long> request) {
        try {
            Long courseId = request.get("courseId");
            Long studentId = request.get("studentId");
            if (studentId == null) {
                studentId = 1L;
            }
            courseService.selectCourse(courseId, studentId);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/drop")
    public Result<Void> dropCourse(@RequestBody Map<String, Long> request) {
        try {
            Long courseId = request.get("courseId");
            Long studentId = request.get("studentId");
            courseService.dropCourse(courseId, studentId);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/{courseId}/students")
    public Result<List<Map<String, Object>>> getCourseStudents(@PathVariable Long courseId) {
        try {
            List<Map<String, Object>> students = courseService.getCourseStudents(courseId);
            return Result.success(students);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/create")
    public Result<Course> createCourse(@RequestBody Course course) {
        try {
            Course createdCourse = courseService.createCourse(course);
            return Result.success(createdCourse);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{courseId}")
    public Result<Course> updateCourse(@PathVariable Long courseId, @RequestBody Course input) {
        try {
            Course updated = courseService.updateCourse(courseId, input);
            return Result.success(updated);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteCourse(@PathVariable Long id) {
        try {
            courseService.deleteCourse(id);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ==================== 评论管理 ====================

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

    @GetMapping("/{courseId}/progress/summary")
    public Result<Map<String, Object>> getCourseProgressSummary(
            @PathVariable Long courseId,
            @RequestParam Long userId,
            @RequestParam String role) {
        try {
            return Result.success(courseService.getCourseProgressSummary(courseId, userId, role));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{courseId}/chapters")
    public Result<CourseChapter> saveChapter(
            @PathVariable Long courseId,
            @RequestParam Long teacherId,
            @RequestBody CourseChapter chapter) {
        try {
            return Result.success(courseService.saveChapter(courseId, teacherId, chapter));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/chapters/{chapterId}")
    public Result<CourseChapter> updateChapter(
            @PathVariable Long chapterId,
            @RequestParam Long teacherId,
            @RequestBody CourseChapter chapter) {
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

    @PostMapping("/{courseId}/videos")
    public Result<CourseVideo> saveVideo(
            @PathVariable Long courseId,
            @RequestParam Long teacherId,
            @RequestBody CourseVideo video) {
        try {
            return Result.success(courseService.saveVideo(courseId, teacherId, video));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/videos/{videoId}")
    public Result<CourseVideo> updateVideo(
            @PathVariable Long videoId,
            @RequestParam Long teacherId,
            @RequestBody CourseVideo video) {
        try {
            return Result.success(courseService.updateVideo(videoId, teacherId, video));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/videos/{videoId}")
    public Result<Void> deleteVideo(@PathVariable Long videoId, @RequestParam Long teacherId) {
        try {
            courseService.deleteVideo(videoId, teacherId);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/videos/{videoId}/play-info")
    public Result<Map<String, Object>> getVideoPlayInfo(
            @PathVariable Long videoId,
            @RequestParam Long userId,
            @RequestParam String role) {
        try {
            return Result.success(courseService.getVideoPlayInfo(videoId, userId, role));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{courseId}/materials")
    public Result<CourseMaterial> saveMaterial(
            @PathVariable Long courseId,
            @RequestParam Long teacherId,
            @RequestBody CourseMaterial material) {
        try {
            return Result.success(courseService.saveMaterial(courseId, teacherId, material));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/materials/{id}")
    public Result<CourseMaterial> updateMaterial(
            @PathVariable Long id,
            @RequestParam Long teacherId,
            @RequestBody CourseMaterial material) {
        try {
            return Result.success(courseService.updateMaterial(id, teacherId, material));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/materials/{id}")
    public Result<Void> deleteMaterial(@PathVariable Long id, @RequestParam Long teacherId) {
        try {
            courseService.deleteMaterial(id, teacherId);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{courseId}/announcements")
    public Result<CourseAnnouncement> saveAnnouncement(
            @PathVariable Long courseId,
            @RequestParam Long teacherId,
            @RequestBody CourseAnnouncement announcement) {
        try {
            return Result.success(courseService.saveAnnouncement(courseId, teacherId, announcement));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/announcements/{id}")
    public Result<CourseAnnouncement> updateAnnouncement(
            @PathVariable Long id,
            @RequestParam Long teacherId,
            @RequestBody CourseAnnouncement announcement) {
        try {
            return Result.success(courseService.updateAnnouncement(id, teacherId, announcement));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/announcements/{id}")
    public Result<Void> deleteAnnouncement(@PathVariable Long id, @RequestParam Long teacherId) {
        try {
            courseService.deleteAnnouncement(id, teacherId);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/videos/{videoId}/progress")
    public Result<VideoProgress> getVideoProgress(
            @PathVariable Long videoId,
            @RequestParam Long studentId) {
        try {
            return Result.success(courseService.getVideoProgress(videoId, studentId));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/videos/{videoId}/progress")
    public Result<VideoProgress> saveVideoProgress(
            @PathVariable Long videoId,
            @RequestParam Long studentId,
            @RequestBody Map<String, Object> request) {
        try {
            Integer lastPosition = request.get("lastPosition") == null
                    ? 0 : Integer.valueOf(request.get("lastPosition").toString());
            Integer duration = request.get("duration") == null
                    ? 0 : Integer.valueOf(request.get("duration").toString());
            return Result.success(courseService.saveVideoProgress(videoId, studentId, lastPosition, duration));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/videos/{videoId}/danmaku")
    public Result<List<VideoDanmaku>> getVideoDanmaku(
            @PathVariable Long videoId,
            @RequestParam Long userId,
            @RequestParam String role) {
        try {
            return Result.success(courseService.getVideoDanmaku(videoId, userId, role));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/videos/{videoId}/danmaku")
    public Result<VideoDanmaku> addVideoDanmaku(
            @PathVariable Long videoId,
            @RequestParam Long studentId,
            @RequestBody VideoDanmaku danmaku) {
        try {
            danmaku.setStudentId(studentId);
            return Result.success(courseService.addVideoDanmaku(videoId, danmaku));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/{courseId}/comments")
    public Result<List<Map<String, Object>>> getCourseComments(@PathVariable Long courseId) {
        try {
            List<Map<String, Object>> comments = courseService.getCourseComments(courseId);
            return Result.success(comments);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{courseId}/comments")
    public Result<CourseComment> addComment(
            @PathVariable Long courseId,
            @RequestBody Map<String, Object> commentData) {
        try {
            CourseComment comment = new CourseComment();
            comment.setCourseId(courseId);
            comment.setUserId(Long.valueOf(commentData.get("userId").toString()));
            comment.setUsername(commentData.get("username").toString());
            comment.setUserName(commentData.get("userName").toString());
            comment.setUserRole(commentData.get("userRole").toString());

            if (commentData.get("parentId") != null) {
                comment.setParentId(Long.valueOf(commentData.get("parentId").toString()));
            }

            comment.setContent(commentData.get("content").toString());

            if (commentData.get("rating") != null) {
                comment.setRating(Integer.valueOf(commentData.get("rating").toString()));
            }

            CourseComment saved = courseService.addComment(comment);
            return Result.success(saved);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/comments/{commentId}")
    public Result<Void> deleteComment(@PathVariable Long commentId) {
        try {
            courseService.deleteComment(commentId);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

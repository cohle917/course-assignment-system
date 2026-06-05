package com.teaching.course.service;

import com.teaching.common.entity.Course;
import com.teaching.common.entity.CourseComment;
import com.teaching.common.entity.CourseSelection;
import com.teaching.course.repository.CourseCommentRepository;
import com.teaching.course.repository.CourseRepository;
import com.teaching.course.repository.CourseSelectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseSelectionRepository courseSelectionRepository;
    private final CourseCommentRepository courseCommentRepository;

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

    public void deleteCourse(Long id) {
        courseRepository.deleteById(id);
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

    private void deleteCommentWithReplies(Long parentId) {
        List<CourseComment> replies = courseCommentRepository.findByParentIdOrderByCreatedAtAsc(parentId);
        for (CourseComment reply : replies) {
            deleteCommentWithReplies(reply.getId());
        }
        courseCommentRepository.deleteById(parentId);
    }
}

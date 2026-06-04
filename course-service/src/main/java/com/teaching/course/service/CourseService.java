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

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {
    
    private final CourseRepository courseRepository;
    private final CourseSelectionRepository courseSelectionRepository;
    private final CourseCommentRepository courseCommentRepository;
    
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
    
    @Transactional
    public void selectCourse(Long courseId, Long studentId) {
        if (courseSelectionRepository.existsByCourseIdAndStudentId(courseId, studentId)) {
            throw new RuntimeException("已经选过该课程");
        }
        
        CourseSelection selection = new CourseSelection();
        selection.setCourseId(courseId);
        selection.setStudentId(studentId);
        courseSelectionRepository.save(selection);
    }
    
    public List<Map<String, Object>> getCourseStudents(Long courseId) {
        List<CourseSelection> selections = courseSelectionRepository.findByCourseId(courseId);
        return selections.stream().map(s -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", s.getStudentId());
            map.put("username", "student" + s.getStudentId());
            map.put("name", "学生" + s.getStudentId());
            map.put("email", "student" + s.getStudentId() + "@example.com");
            return map;
        }).collect(Collectors.toList());
    }
    
    public Course createCourse(Course course) {
        return courseRepository.save(course);
    }
    
    public void deleteCourse(Long id) {
        courseRepository.deleteById(id);
    }
    
    // ========== 评论相关方法 ==========
    
    public List<Map<String, Object>> getCourseComments(Long courseId) {
        // 获取所有评论
        List<CourseComment> allComments = courseCommentRepository.findByCourseIdOrderByCreatedAtDesc(courseId);
        
        // 构建评论树结构
        List<Map<String, Object>> rootComments = new ArrayList<>();
        Map<Long, List<Map<String, Object>>> replyMap = new HashMap<>();
        
        // 先将所有评论按parentId分组
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
            commentMap.put("createdAt", comment.getCreatedAt());
            commentMap.put("replyCount", courseCommentRepository.countByParentId(comment.getId()));
            
            if (comment.getParentId() == null) {
                // 根评论
                rootComments.add(commentMap);
            } else {
                // 回复，按parentId分组
                replyMap.computeIfAbsent(comment.getParentId(), k -> new ArrayList<>()).add(commentMap);
            }
        }
        
        // 为每个根评论设置回复列表
        for (Map<String, Object> rootComment : rootComments) {
            Long rootId = (Long) rootComment.get("id");
            List<Map<String, Object>> replies = replyMap.getOrDefault(rootId, new ArrayList<>());
            rootComment.put("replies", replies);
        }
        
        return rootComments;
    }
    
    public CourseComment addComment(CourseComment comment) {
        return courseCommentRepository.save(comment);
    }
    
    public void deleteComment(Long commentId) {
        // 删除评论及其所有回复
        deleteCommentWithReplies(commentId);
    }
    
    private void deleteCommentWithReplies(Long parentId) {
        // 先删除所有子回复
        List<CourseComment> replies = courseCommentRepository.findByParentIdOrderByCreatedAtAsc(parentId);
        for (CourseComment reply : replies) {
            deleteCommentWithReplies(reply.getId());
        }
        // 再删除当前评论
        courseCommentRepository.deleteById(parentId);
    }
}

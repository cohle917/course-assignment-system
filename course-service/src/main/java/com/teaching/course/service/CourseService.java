package com.teaching.course.service;

import com.teaching.common.entity.Course;
import com.teaching.common.entity.CourseSelection;
import com.teaching.course.repository.CourseRepository;
import com.teaching.course.repository.CourseSelectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {
    
    private final CourseRepository courseRepository;
    private final CourseSelectionRepository courseSelectionRepository;
    
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
}

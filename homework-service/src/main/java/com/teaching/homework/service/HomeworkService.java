package com.teaching.homework.service;

import com.teaching.common.entity.Homework;
import com.teaching.common.entity.HomeworkSubmission;
import com.teaching.homework.repository.HomeworkRepository;
import com.teaching.homework.repository.HomeworkSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HomeworkService {
    
    private final HomeworkRepository homeworkRepository;
    private final HomeworkSubmissionRepository submissionRepository;
    
    public List<Homework> getAllHomeworks() {
        return homeworkRepository.findAll();
    }
    
    public List<Homework> getHomeworksByCourse(Long courseId) {
        return homeworkRepository.findByCourseId(courseId);
    }
    
    public List<Map<String, Object>> getHomeworksByStudent(Long studentId) {
        List<HomeworkSubmission> submissions = submissionRepository.findByStudentId(studentId);
        List<Long> submittedHomeworkIds = submissions.stream()
                .map(HomeworkSubmission::getHomeworkId)
                .collect(Collectors.toList());
        
        List<Homework> allHomeworks = homeworkRepository.findAll();
        
        return allHomeworks.stream().map(homework -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", homework.getId());
            map.put("title", homework.getTitle());
            map.put("courseName", homework.getCourseName());
            map.put("deadline", homework.getDeadline());
            map.put("description", homework.getDescription());
            
            boolean isSubmitted = submittedHomeworkIds.contains(homework.getId());
            map.put("status", isSubmitted ? "已完成" : "未完成");
            
            return map;
        }).collect(Collectors.toList());
    }
    
    @Transactional
    public void submitHomework(Long homeworkId, Long studentId, String content) {
        if (submissionRepository.findByHomeworkIdAndStudentId(homeworkId, studentId).isPresent()) {
            throw new RuntimeException("已经提交过该作业");
        }
        
        HomeworkSubmission submission = new HomeworkSubmission();
        submission.setHomeworkId(homeworkId);
        submission.setStudentId(studentId);
        submission.setStudentName("学生" + studentId);
        submission.setContent(content);
        submissionRepository.save(submission);
    }
    
    public Homework createHomework(Homework homework) {
        return homeworkRepository.save(homework);
    }
    
    public List<HomeworkSubmission> getSubmissionsByHomework(Long homeworkId) {
        return submissionRepository.findByHomeworkId(homeworkId);
    }
    
    public List<Map<String, Object>> getHomeworkSubmissions(Long homeworkId) {
        List<HomeworkSubmission> submissions = submissionRepository.findByHomeworkId(homeworkId);
        return submissions.stream().map(s -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", s.getId());
            map.put("studentId", s.getStudentId());
            map.put("studentName", s.getStudentName());
            map.put("content", s.getContent());
            map.put("submitTime", s.getSubmitTime());
            return map;
        }).collect(Collectors.toList());
    }
}

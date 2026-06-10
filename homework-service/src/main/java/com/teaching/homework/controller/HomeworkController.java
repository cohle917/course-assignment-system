package com.teaching.homework.controller;

import com.teaching.common.entity.Homework;
import com.teaching.common.result.Result;
import com.teaching.homework.service.HomeworkService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/homework")
@CrossOrigin(origins = "*")
public class HomeworkController {

    private final HomeworkService homeworkService;

    public HomeworkController(HomeworkService homeworkService) {
        this.homeworkService = homeworkService;
    }
    
    @GetMapping("/list")
    public Result<List<Homework>> getAllHomeworks() {
        try {
            List<Homework> homeworks = homeworkService.getAllHomeworks();
            return Result.success(homeworks);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/my-homeworks")
    public Result<List<Map<String, Object>>> getMyHomeworks(@RequestParam Long studentId) {
        try {
            List<Map<String, Object>> homeworks = homeworkService.getHomeworksByStudent(studentId);
            return Result.success(homeworks);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping("/submit")
    public Result<Void> submitHomework(@RequestBody Map<String, Object> request) {
        try {
            Long homeworkId = Long.valueOf(request.get("homeworkId").toString());
            Long studentId = request.get("studentId") != null ? 
                    Long.valueOf(request.get("studentId").toString()) : 1L;
            String content = request.get("content").toString();
            
            homeworkService.submitHomework(homeworkId, studentId, content);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping("/publish")
    public Result<Homework> publishHomework(@RequestBody Homework homework) {
        try {
            Homework created = homeworkService.createHomework(homework);
            return Result.success(created);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/{homeworkId}/submissions")
    public Result<List<Map<String, Object>>> getHomeworkSubmissions(@PathVariable Long homeworkId) {
        try {
            List<Map<String, Object>> submissions = homeworkService.getHomeworkSubmissions(homeworkId);
            return Result.success(submissions);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

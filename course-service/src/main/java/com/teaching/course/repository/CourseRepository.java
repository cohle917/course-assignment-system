package com.teaching.course.repository;

import com.teaching.common.entity.Course;
import com.teaching.common.entity.Course.CourseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByTeacherId(Long teacherId);

    List<Course> findByNameContainingOrDescriptionContaining(String nameKeyword, String descKeyword);

    List<Course> findByCategory(String category);

    List<Course> findByDepartment(String department);

    List<Course> findBySemester(String semester);

    List<Course> findByStatus(CourseStatus status);

    @Query("SELECT DISTINCT c.category FROM Course c WHERE c.category IS NOT NULL AND c.category <> ''")
    List<String> findDistinctCategories();

    @Query("SELECT DISTINCT c.department FROM Course c WHERE c.department IS NOT NULL AND c.department <> ''")
    List<String> findDistinctDepartments();

    @Query("SELECT DISTINCT c.semester FROM Course c WHERE c.semester IS NOT NULL AND c.semester <> ''")
    List<String> findDistinctSemesters();
}

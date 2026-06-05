package com.teaching.course.repository;

import com.teaching.common.entity.CourseComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseCommentRepository extends JpaRepository<CourseComment, Long> {

    List<CourseComment> findByCourseIdOrderByCreatedAtDesc(Long courseId);

    List<CourseComment> findByParentIdOrderByCreatedAtAsc(Long parentId);

    List<CourseComment> findByCourseIdAndParentIdIsNullOrderByCreatedAtDesc(Long courseId);

    int countByCourseId(Long courseId);

    int countByParentId(Long parentId);

    long countByCourseIdAndParentIdIsNull(Long courseId);

    @Query("SELECT AVG(c.rating) FROM CourseComment c WHERE c.courseId = :courseId AND c.rating IS NOT NULL")
    Double findAverageRatingByCourseId(Long courseId);
}

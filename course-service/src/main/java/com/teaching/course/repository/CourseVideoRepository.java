package com.teaching.course.repository;

import com.teaching.common.entity.CourseVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseVideoRepository extends JpaRepository<CourseVideo, Long> {
    List<CourseVideo> findByCourseIdOrderBySortOrderAscIdAsc(Long courseId);

    List<CourseVideo> findByChapterIdOrderBySortOrderAscIdAsc(Long chapterId);

    Optional<CourseVideo> findByIdAndCourseId(Long id, Long courseId);
}

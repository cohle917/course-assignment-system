package com.teaching.course.repository;

import com.teaching.common.entity.CourseChapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseChapterRepository extends JpaRepository<CourseChapter, Long> {
    List<CourseChapter> findByCourseIdOrderBySortOrderAscIdAsc(Long courseId);

    List<CourseChapter> findByCourseIdAndParentIdOrderBySortOrderAscIdAsc(Long courseId, Long parentId);
}

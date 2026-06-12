package com.teaching.course.repository;

import com.teaching.common.entity.CourseMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseMaterialRepository extends JpaRepository<CourseMaterial, Long> {
    List<CourseMaterial> findByCourseIdOrderBySortOrderAscIdAsc(Long courseId);

    List<CourseMaterial> findByChapterIdOrderBySortOrderAscIdAsc(Long chapterId);
}

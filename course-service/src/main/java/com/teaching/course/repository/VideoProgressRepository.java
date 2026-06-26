package com.teaching.course.repository;

import com.teaching.common.entity.VideoProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface VideoProgressRepository extends JpaRepository<VideoProgress, Long> {
    Optional<VideoProgress> findByStudentIdAndVideoId(Long studentId, Long videoId);

    List<VideoProgress> findByVideoIdIn(List<Long> videoIds);
}

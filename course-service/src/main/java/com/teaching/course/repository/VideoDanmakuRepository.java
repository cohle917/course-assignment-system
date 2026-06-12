package com.teaching.course.repository;

import com.teaching.common.entity.VideoDanmaku;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoDanmakuRepository extends JpaRepository<VideoDanmaku, Long> {
    List<VideoDanmaku> findByVideoIdAndStatusOrderByTimeSecondsAscCreatedAtAsc(
            Long videoId, VideoDanmaku.DanmakuStatus status);
}

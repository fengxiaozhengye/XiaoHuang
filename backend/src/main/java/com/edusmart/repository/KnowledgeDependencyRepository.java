package com.edusmart.repository;

import com.edusmart.entity.KnowledgeDependency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface KnowledgeDependencyRepository extends JpaRepository<KnowledgeDependency, Long> {

    @Query("SELECT d FROM KnowledgeDependency d WHERE d.source.course.id = :courseId OR d.target.course.id = :courseId")
    List<KnowledgeDependency> findByCourseId(@Param("courseId") Long courseId);

    List<KnowledgeDependency> findBySourceId(Long sourceId);

    List<KnowledgeDependency> findByTargetId(Long targetId);
}

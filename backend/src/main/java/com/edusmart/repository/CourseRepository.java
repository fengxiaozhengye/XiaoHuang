package com.edusmart.repository;

import com.edusmart.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseRepository extends JpaRepository<Course, Long> {

    @Query("SELECT c FROM Course c WHERE (:keyword IS NULL OR c.name LIKE %:keyword%) AND c.status = 'PUBLISHED'")
    Page<Course> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
}

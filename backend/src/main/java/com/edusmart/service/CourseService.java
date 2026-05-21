package com.edusmart.service;

import com.edusmart.common.BusinessException;
import com.edusmart.entity.Course;
import com.edusmart.entity.User;
import com.edusmart.repository.CourseRepository;
import com.edusmart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public Page<Course> listCourses(String keyword, int page, int size) {
        return courseRepository.findByKeyword(keyword,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    public Course getCourse(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new BusinessException("课程不存在"));
    }

    public Course createCourse(Long creatorId, String name, String description, String subjectArea) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        Course course = new Course();
        course.setName(name);
        course.setDescription(description);
        course.setSubjectArea(subjectArea);
        course.setCreator(creator);
        course.setStatus("DRAFT");
        return courseRepository.save(course);
    }

    public Course publishCourse(Long id) {
        Course course = getCourse(id);
        course.setStatus("PUBLISHED");
        return courseRepository.save(course);
    }
}

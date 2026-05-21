package com.edusmart.controller;

import com.edusmart.common.Result;
import com.edusmart.entity.Course;
import com.edusmart.entity.User;
import com.edusmart.repository.UserRepository;
import com.edusmart.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "课程管理模块")
@RestController
@RequestMapping("/api/course")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final UserRepository userRepository;

    @Operation(summary = "课程列表")
    @GetMapping("/list")
    public Result<Page<Course>> listCourses(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(courseService.listCourses(keyword, page, size));
    }

    @Operation(summary = "课程详情")
    @GetMapping("/{id}")
    public Result<Course> getCourse(@PathVariable Long id) {
        return Result.success(courseService.getCourse(id));
    }

    @Operation(summary = "创建课程")
    @PostMapping
    public Result<Course> createCourse(Authentication authentication, @RequestBody Map<String, String> body) {
        Long userId = getUserId(authentication);
        return Result.success(courseService.createCourse(userId,
                body.get("name"), body.get("description"), body.get("subjectArea")));
    }

    @Operation(summary = "发布课程")
    @PutMapping("/{id}/publish")
    public Result<Course> publishCourse(@PathVariable Long id) {
        return Result.success(courseService.publishCourse(id));
    }

    private Long getUserId(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        return user.getId();
    }
}

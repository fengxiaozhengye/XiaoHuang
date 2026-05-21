package com.edusmart.controller;

import com.edusmart.common.Result;
import com.edusmart.entity.User;
import com.edusmart.repository.UserRepository;
import com.edusmart.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "学习分析模块")
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final UserRepository userRepository;

    @Operation(summary = "学习概览")
    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview(Authentication authentication) {
        Long userId = getUserId(authentication);
        return Result.success(analyticsService.getOverview(userId));
    }

    @Operation(summary = "课程进度")
    @GetMapping("/progress")
    public Result<Map<String, Object>> getProgress(Authentication authentication,
                                                    @RequestParam Long courseId) {
        Long userId = getUserId(authentication);
        return Result.success(analyticsService.getProgress(userId, courseId));
    }

    private Long getUserId(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        return user.getId();
    }
}

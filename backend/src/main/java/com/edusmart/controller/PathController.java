package com.edusmart.controller;

import com.edusmart.common.Result;
import com.edusmart.entity.LearningPath;
import com.edusmart.entity.User;
import com.edusmart.repository.UserRepository;
import com.edusmart.service.PathService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "学习路径模块")
@RestController
@RequestMapping("/api/path")
@RequiredArgsConstructor
public class PathController {

    private final PathService pathService;
    private final UserRepository userRepository;

    @Operation(summary = "生成学习路径")
    @PostMapping("/generate")
    public Result<LearningPath> generatePath(Authentication authentication, @RequestBody Map<String, Object> body) {
        Long userId = getUserId(authentication);
        Long courseId = ((Number) body.get("courseId")).longValue();
        return Result.success(pathService.generatePath(userId, courseId));
    }

    @Operation(summary = "获取当前学习路径")
    @GetMapping("/active")
    public Result<LearningPath> getActivePath(Authentication authentication,
                                               @RequestParam Long courseId) {
        Long userId = getUserId(authentication);
        return Result.success(pathService.getActivePath(userId, courseId));
    }

    @Operation(summary = "获取用户所有学习路径")
    @GetMapping("/list")
    public Result<List<LearningPath>> getUserPaths(Authentication authentication) {
        Long userId = getUserId(authentication);
        return Result.success(pathService.getUserPaths(userId));
    }

    @Operation(summary = "推进学习步骤")
    @PutMapping("/{pathId}/next")
    public Result<LearningPath> advanceStep(@PathVariable Long pathId) {
        return Result.success(pathService.advanceStep(pathId));
    }

    private Long getUserId(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        return user.getId();
    }
}

package com.edusmart.controller;

import com.edusmart.common.Result;
import com.edusmart.entity.StudentProfile;
import com.edusmart.entity.User;
import com.edusmart.repository.UserRepository;
import com.edusmart.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "学生画像模块")
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final UserRepository userRepository;

    @Operation(summary = "获取学生画像")
    @GetMapping("/{userId}")
    public Result<StudentProfile> getProfile(@PathVariable Long userId) {
        return Result.success(profileService.getProfile(userId));
    }

    @Operation(summary = "获取当前用户画像")
    @GetMapping("/me")
    public Result<StudentProfile> getMyProfile(Authentication authentication) {
        Long userId = getUserId(authentication);
        return Result.success(profileService.getProfile(userId));
    }

    @Operation(summary = "创建或更新画像")
    @PutMapping("/{userId}")
    public Result<StudentProfile> updateProfile(@PathVariable Long userId,
                                                 @RequestBody Map<String, Object> profileData) {
        return Result.success(profileService.createOrUpdateProfile(userId, profileData));
    }

    @Operation(summary = "画像概览")
    @GetMapping("/{userId}/summary")
    public Result<Map<String, Object>> getProfileSummary(@PathVariable Long userId) {
        return Result.success(profileService.getProfileSummary(userId));
    }

    private Long getUserId(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow();
        return user.getId();
    }
}

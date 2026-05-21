package com.edusmart.controller;

import com.edusmart.common.JwtUtil;
import com.edusmart.common.Result;
import com.edusmart.dto.request.LoginRequest;
import com.edusmart.dto.request.RegisterRequest;
import com.edusmart.dto.response.LoginResponse;
import com.edusmart.entity.User;
import com.edusmart.repository.UserRepository;
import com.edusmart.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "认证模块")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success(userService.register(request));
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(userService.login(request));
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/me")
    public Result<Map<String, Object>> getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow();
        return Result.success(Map.of(
                "userId", user.getId(),
                "username", user.getUsername(),
                "role", user.getRole().name(),
                "email", user.getEmail() != null ? user.getEmail() : "",
                "realName", user.getRealName() != null ? user.getRealName() : ""
        ));
    }
}

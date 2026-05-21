package com.edusmart.controller;

import com.edusmart.common.Result;
import com.edusmart.entity.LearningResource;
import com.edusmart.enums.ResourceType;
import com.edusmart.service.ResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "学习资源模块")
@RestController
@RequestMapping("/api/resource")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    @Operation(summary = "获取知识点资源列表")
    @GetMapping("/list")
    public Result<List<LearningResource>> getResources(
            @RequestParam Long knowledgePointId,
            @RequestParam(required = false) ResourceType type,
            @RequestParam(required = false) Integer difficulty) {
        return Result.success(resourceService.getResources(knowledgePointId, type, difficulty));
    }

    @Operation(summary = "获取资源详情")
    @GetMapping("/{id}")
    public Result<LearningResource> getResource(@PathVariable Long id) {
        return Result.success(resourceService.getResource(id));
    }

    @Operation(summary = "AI生成学习资源")
    @PostMapping("/generate")
    public Result<LearningResource> generateResource(@RequestBody Map<String, Object> body) {
        Long kpId = ((Number) body.get("knowledgePointId")).longValue();
        ResourceType type = ResourceType.valueOf((String) body.getOrDefault("type", "TEXT"));
        Integer difficulty = body.containsKey("difficulty") ? ((Number) body.get("difficulty")).intValue() : 3;
        String studentLevel = (String) body.get("studentLevel");
        return Result.success(resourceService.generateResource(kpId, type, difficulty, studentLevel));
    }
}

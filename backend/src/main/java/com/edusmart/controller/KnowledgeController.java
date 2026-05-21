package com.edusmart.controller;

import com.edusmart.common.Result;
import com.edusmart.entity.KnowledgeDependency;
import com.edusmart.entity.KnowledgePoint;
import com.edusmart.service.KnowledgeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "知识图谱模块")
@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    @Operation(summary = "获取课程知识点列表")
    @GetMapping("/course/{courseId}")
    public Result<List<KnowledgePoint>> getKnowledgePoints(@PathVariable Long courseId) {
        return Result.success(knowledgeService.getKnowledgePointsByCourse(courseId));
    }

    @Operation(summary = "获取知识点详情")
    @GetMapping("/{id}")
    public Result<KnowledgePoint> getKnowledgePoint(@PathVariable Long id) {
        return Result.success(knowledgeService.getKnowledgePoint(id));
    }

    @Operation(summary = "获取知识点依赖关系")
    @GetMapping("/course/{courseId}/dependencies")
    public Result<List<KnowledgeDependency>> getDependencies(@PathVariable Long courseId) {
        return Result.success(knowledgeService.getDependenciesByCourse(courseId));
    }

    @Operation(summary = "导入知识点和依赖关系")
    @PostMapping("/course/{courseId}/import")
    public Result<Map<String, Object>> importKnowledge(
            @PathVariable Long courseId,
            @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> points = (List<Map<String, Object>>) body.get("points");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> dependencies = (List<Map<String, Object>>) body.get("dependencies");
        return Result.success(knowledgeService.importKnowledge(courseId, points, dependencies));
    }

    @Operation(summary = "获取课程知识图谱（节点+边）")
    @GetMapping("/graph/{courseId}")
    public Result<Map<String, Object>> getKnowledgeGraph(@PathVariable Long courseId) {
        return Result.success(knowledgeService.getKnowledgeGraph(courseId));
    }
}

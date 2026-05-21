# EduSmart - 基于大模型的个性化资源生成与学习多智能体系统

## 项目简介
面向高等教育的个性化学习资源生成与多智能体辅导系统，基于 Spring Boot 3 + Spring AI + React + Ant Design 构建。

## 技术栈
- 后端: JDK 17 / Spring Boot 3.2 / Spring AI / MySQL / PostgreSQL+pgvector / Redis / MinIO
- 前端: React 18 / TypeScript / Ant Design 5 / Zustand / Axios / markdown-it

## Git 提交规范

每次功能开发或修复完成后，必须按照以下规范提交：

### 提交类型
- `feat`: 新功能（功能更新）
- `fix`: Bug修复（功能修复）
- `refactor`: 重构（不影响功能和修复）
- `docs`: 文档更新
- `test`: 测试相关
- `chore`: 构建/配置变更

### 提交格式
```
<type>(<scope>): <简短描述>

<详细说明（可选）>
```

### 版本号规范
采用语义化版本 `vX.Y.Z`：
- X (主版本): 重大功能变更或架构调整
- Y (次版本): 新功能模块上线
- Z (修订版本): Bug修复和小优化

### 提交流程
1. 完成功能开发
2. 运行测试确认通过
3. `git add` 相关文件（不要用 `git add -A`）
4. 编写符合规范的提交信息
5. 每完成一个功能模块，打版本标签：`git tag vX.Y.Z`

## 目录结构
- `backend/` - Spring Boot 后端
- `frontend/` - React 前端
- `项目设计规范文档.md` - 完整设计文档

## 测试要求
- 后端: 每个模块编写完成后，通过 Swagger 手动测试 + 单元测试
- 前端: 每个页面编写完成后，浏览器手动验证功能

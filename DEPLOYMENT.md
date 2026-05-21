# EduSmart 部署文档

## 目录

- [环境要求](#环境要求)
- [数据库服务部署](#数据库服务部署)
- [后端部署](#后端部署)
- [前端部署](#前端部署)
- [生产环境配置](#生产环境配置)
- [验证部署](#验证部署)
- [常见问题](#常见问题)

---

## 环境要求

### 必需软件

| 软件 | 版本 | 用途 |
|------|------|------|
| JDK | 17+ | 后端运行环境 |
| Maven | 3.9+ | 后端构建工具 |
| Node.js | 18+ | 前端运行环境 |
| MySQL | 8.0+ | 主业务数据库 |
| PostgreSQL | 15+ (含pgvector扩展) | 向量数据库 |
| Redis | 7.x | 缓存服务 |
| MinIO | 最新 | 对象存储服务 |

### 可选软件

| 软件 | 用途 |
|------|------|
| Docker | 容器化部署（推荐） |
| Nginx | 反向代理（生产环境） |

---

## 数据库服务部署

### 1. MySQL

#### 安装

**Windows:**
```bash
# 下载MySQL 8.0安装包
# https://dev.mysql.com/downloads/installer/
```

**Linux (Ubuntu):**
```bash
sudo apt update
sudo apt install mysql-server
sudo systemctl start mysql
sudo systemctl enable mysql
```

**Docker:**
```bash
docker run -d \
  --name mysql \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=123456 \
  -e MYSQL_DATABASE=edusmart \
  -v mysql_data:/var/lib/mysql \
  mysql:8.0
```

#### 初始化数据库

```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS edusmart DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户（可选，生产环境建议）
CREATE USER 'edusmart'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON edusmart.* TO 'edusmart'@'localhost';
FLUSH PRIVILEGES;
```

### 2. PostgreSQL + pgvector

#### 安装

**Ubuntu:**
```bash
sudo apt install postgresql postgresql-contrib
sudo systemctl start postgresql

# 安装pgvector扩展
sudo apt install postgresql-15-pgvector
```

**Docker:**
```bash
docker run -d \
  --name postgres \
  -p 5432:5432 \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=edusmart_vector \
  -v pg_data:/var/lib/postgresql/data \
  pgvector/pgvector:pg15
```

#### 初始化向量数据库

```sql
-- 连接到PostgreSQL
psql -U postgres

-- 创建数据库
CREATE DATABASE edusmart_vector;

-- 连接到新数据库
\c edusmart_vector

-- 启用向量扩展
CREATE EXTENSION IF NOT EXISTS vector;

-- 创建向量表
CREATE TABLE document_embedding (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    course_id BIGINT,
    document_id BIGINT,
    knowledge_point_id BIGINT,
    content TEXT NOT NULL,
    metadata JSONB,
    embedding vector(1024),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建向量索引
CREATE INDEX idx_embedding ON document_embedding
    USING ivfflat (embedding vector_cosine_ops)
    WITH (lists = 100);

-- 创建用户课程索引
CREATE INDEX idx_user_course ON document_embedding (user_id, course_id);
CREATE INDEX idx_document ON document_embedding (document_id);
```

### 3. Redis

#### 安装

**Ubuntu:**
```bash
sudo apt install redis-server
sudo systemctl start redis
sudo systemctl enable redis
```

**Docker:**
```bash
docker run -d \
  --name redis \
  -p 6379:6379 \
  -v redis_data:/data \
  redis:7-alpine
```

#### 验证

```bash
redis-cli ping
# 返回 PONG 表示正常
```

### 4. MinIO

#### 安装

**Docker (推荐):**
```bash
docker run -d \
  --name minio \
  -p 9000:9000 \
  -p 9001:9001 \
  -e MINIO_ROOT_USER=minioadmin \
  -e MINIO_ROOT_PASSWORD=minioadmin \
  -v minio_data:/data \
  minio/minio server /data --console-address ":9001"
```

#### 初始化存储桶

1. 访问 MinIO Console: http://localhost:9001
2. 使用 `minioadmin/minioadmin` 登录
3. 创建存储桶 `edusmart-resources`
4. 设置访问策略为 `public` 或 `readwrite`

---

## 后端部署

### 1. 配置环境变量

创建环境变量文件或设置系统环境变量：

```bash
# AI模型配置（必需）
export AI_API_KEY="your_dashscope_api_key"
export AI_BASE_URL="https://dashscope.aliyuncs.com/compatible-mode/v1"
export AI_MODEL="qwen-plus"

# MySQL配置
export MYSQL_PASSWORD="123456"

# PostgreSQL配置
export PG_USERNAME="postgres"
export PG_PASSWORD="postgres"

# MinIO配置
export MINIO_ACCESS_KEY="minioadmin"
export MINIO_SECRET_KEY="minioadmin"

# JWT密钥（生产环境必须修改）
export JWT_SECRET="your-256-bit-secret-key-must-be-long-enough"
```

### 2. 修改配置文件

编辑 `backend/src/main/resources/application-dev.yml`：

```yaml
spring:
  # MySQL
  datasource:
    url: jdbc:mysql://your-mysql-host:3306/edusmart?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8mb4&allowPublicKeyRetrieval=true
    username: root
    password: ${MYSQL_PASSWORD:123456}

  # Spring AI
  ai:
    openai:
      api-key: ${AI_API_KEY}
      base-url: ${AI_BASE_URL:https://dashscope.aliyuncs.com/compatible-mode/v1}
      chat:
        options:
          model: ${AI_MODEL:qwen-plus}

# PostgreSQL
pgvector:
  url: jdbc:postgresql://your-pg-host:5432/edusmart_vector
  username: ${PG_USERNAME:postgres}
  password: ${PG_PASSWORD:postgres}

# MinIO
minio:
  endpoint: http://your-minio-host:9000
  access-key: ${MINIO_ACCESS_KEY:minioadmin}
  secret-key: ${MINIO_SECRET_KEY:minioadmin}
  bucket-name: edusmart-resources

# JWT
jwt:
  secret: ${JWT_SECRET:your-secret-key}
```

### 3. 构建项目

```bash
cd backend

# 清理并构建
mvn clean package -DskipTests

# 或者只编译
mvn clean compile
```

### 4. 启动后端

```bash
# 方式1: 使用Maven启动（开发环境）
mvn spring-boot:run

# 方式2: 使用JAR包启动
java -jar target/edusmart-backend-0.1.0.jar

# 方式3: 指定配置文件
java -jar target/edusmart-backend-0.1.0.jar --spring.profiles.active=dev
```

### 5. 验证后端

访问 Swagger API文档：
- URL: http://localhost:8080/swagger-ui.html
- 应该能看到所有API接口列表

---

## 前端部署

### 1. 安装依赖

```bash
cd frontend

# 安装依赖
npm install

# 或使用yarn
yarn install
```

### 2. 开发环境启动

```bash
# 启动开发服务器
npm run dev

# 或使用yarn
yarn dev
```

前端将运行在 http://localhost:3000

### 3. 生产环境构建

```bash
# 构建生产版本
npm run build

# 或使用yarn
yarn build
```

构建产物将输出到 `frontend/dist/` 目录

### 4. 部署静态文件

**方式1: 使用Nginx**

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 前端静态文件
    location / {
        root /path/to/frontend/dist;
        try_files $uri $uri/ /index.html;
    }

    # API反向代理
    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        
        # SSE支持
        proxy_set_header Connection '';
        proxy_http_version 1.1;
        proxy_buffering off;
        proxy_cache off;
        proxy_read_timeout 60s;
    }
}
```

**方式2: 使用Vite预览**

```bash
npm run preview
```

---

## 生产环境配置

### 1. 创建生产配置文件

创建 `backend/src/main/resources/application-prod.yml`：

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://${MYSQL_HOST:localhost}:3306/edusmart?useSSL=true&serverTimezone=Asia/Shanghai&characterEncoding=utf8mb4
    username: ${MYSQL_USER:edusmart}
    password: ${MYSQL_PASSWORD}
    
  jpa:
    hibernate:
      ddl-auto: validate  # 生产环境使用validate，不要用update
    show-sql: false

  ai:
    openai:
      api-key: ${AI_API_KEY}
      base-url: ${AI_BASE_URL}
      chat:
        options:
          model: ${AI_MODEL:qwen-plus}
          temperature: 0.7
          max-tokens: 4096

pgvector:
  url: jdbc:postgresql://${PG_HOST:localhost}:5432/edusmart_vector
  username: ${PG_USERNAME}
  password: ${PG_PASSWORD}

minio:
  endpoint: http://${MINIO_HOST:localhost}:9000
  access-key: ${MINIO_ACCESS_KEY}
  secret-key: ${MINIO_SECRET_KEY}
  bucket-name: edusmart-resources

jwt:
  secret: ${JWT_SECRET}  # 必须从环境变量读取
  expiration: 86400000

logging:
  level:
    com.edusmart: INFO
    org.springframework.ai: WARN
    org.springframework.web: WARN
```

### 2. Docker Compose 部署（推荐）

创建 `docker-compose.yml`：

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name edusmart-mysql
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD:-123456}
      MYSQL_DATABASE: edusmart
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

  postgres:
    image: pgvector/pgvector:pg15
    container_name edusmart-postgres
    environment:
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: ${PG_PASSWORD:-postgres}
      POSTGRES_DB: edusmart_vector
    ports:
      - "5432:5432"
    volumes:
      - pg_data:/var/lib/postgresql/data
      - ./init-pgvector.sql:/docker-entrypoint-initdb.d/init.sql
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5

  redis:
    image: redis:7-alpine
    container_name edusmart-redis
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5

  minio:
    image: minio/minio
    container_name edusmart-minio
    environment:
      MINIO_ROOT_USER: ${MINIO_USER:-minioadmin}
      MINIO_ROOT_PASSWORD: ${MINIO_PASSWORD:-minioadmin}
    ports:
      - "9000:9000"
      - "9001:9001"
    volumes:
      - minio_data:/data
    command: server /data --console-address ":9001"

  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name edusmart-backend
    environment:
      SPRING_PROFILES_ACTIVE: prod
      MYSQL_HOST: mysql
      MYSQL_PASSWORD: ${MYSQL_ROOT_PASSWORD:-123456}
      PG_HOST: postgres
      PG_PASSWORD: ${PG_PASSWORD:-postgres}
      MINIO_HOST: minio
      AI_API_KEY: ${AI_API_KEY}
      JWT_SECRET: ${JWT_SECRET}
    ports:
      - "8080:8080"
    depends_on:
      mysql:
        condition: service_healthy
      postgres:
        condition: service_healthy
      redis:
        condition: service_healthy
      minio:
        condition: service_started

  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    container_name edusmart-frontend
    ports:
      - "80:80"
    depends_on:
      - backend

volumes:
  mysql_data:
  pg_data:
  redis_data:
  minio_data:
```

创建 `backend/Dockerfile`：

```dockerfile
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

创建 `frontend/Dockerfile`：

```dockerfile
FROM node:18-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

创建 `frontend/nginx.conf`：

```nginx
server {
    listen 80;
    server_name localhost;

    location / {
        root /usr/share/nginx/html;
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://backend:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header Connection '';
        proxy_http_version 1.1;
        proxy_buffering off;
        proxy_cache off;
        proxy_read_timeout 60s;
    }
}
```

创建 `init-pgvector.sql`：

```sql
CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS document_embedding (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    course_id BIGINT,
    document_id BIGINT,
    knowledge_point_id BIGINT,
    content TEXT NOT NULL,
    metadata JSONB,
    embedding vector(1024),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_embedding ON document_embedding
    USING ivfflat (embedding vector_cosine_ops)
    WITH (lists = 100);

CREATE INDEX IF NOT EXISTS idx_user_course ON document_embedding (user_id, course_id);
CREATE INDEX IF NOT EXISTS idx_document ON document_embedding (document_id);
```

创建 `.env` 文件：

```env
# MySQL
MYSQL_ROOT_PASSWORD=your_mysql_password

# PostgreSQL
PG_PASSWORD=your_pg_password

# MinIO
MINIO_USER=minioadmin
MINIO_PASSWORD=your_minio_password

# AI模型（必需）
AI_API_KEY=your_dashscope_api_key

# JWT密钥
JWT_SECRET=your-256-bit-secret-key-here
```

### 3. 启动Docker Compose

```bash
# 构建并启动所有服务
docker-compose up -d --build

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f backend

# 停止服务
docker-compose down

# 停止并删除数据卷
docker-compose down -v
```

---

## 验证部署

### 1. 检查服务状态

```bash
# 检查MySQL
mysql -u root -p -e "SELECT 1"

# 检查PostgreSQL
psql -U postgres -d edusmart_vector -c "SELECT 1"

# 检查Redis
redis-cli ping

# 检查MinIO
curl http://localhost:9000/minio/health/live
```

### 2. 验证后端

```bash
# 健康检查
curl http://localhost:8080/api/health

# Swagger文档
# 浏览器访问: http://localhost:8080/swagger-ui.html
```

### 3. 验证前端

```bash
# 浏览器访问
http://localhost:3000  # 开发环境
http://localhost       # 生产环境(Nginx)
```

### 4. 功能测试

1. **注册/登录测试**
   - 访问 http://localhost:3000/login
   - 注册新用户
   - 登录系统

2. **AI对话测试**
   - 创建对话会话
   - 发送消息
   - 验证SSE流式响应

3. **文档上传测试**
   - 上传PDF/Word文件
   - 检查处理状态
   - 测试语义搜索

---

## 常见问题

### 1. AI_API_KEY未配置

**错误信息：**
```
Failed to bind properties under 'spring.ai.openai.api-key'
```

**解决方案：**
```bash
# 设置环境变量
export AI_API_KEY="your_api_key"

# 或在application-dev.yml中直接配置
spring.ai.openai.api-key: your_api_key
```

### 2. pgvector扩展未安装

**错误信息：**
```
ERROR: type "vector" does not exist
```

**解决方案：**
```sql
-- 在PostgreSQL中执行
CREATE EXTENSION IF NOT EXISTS vector;
```

### 3. MySQL连接失败

**错误信息：**
```
Communications link failure
```

**解决方案：**
```bash
# 检查MySQL是否启动
sudo systemctl status mysql

# 检查防火墙
sudo ufw allow 3306

# 检查MySQL绑定地址
# /etc/mysql/mysql.conf.d/mysqld.cnf
# bind-address = 0.0.0.0
```

### 4. MinIO访问被拒绝

**错误信息：**
```
Access Denied
```

**解决方案：**
1. 登录MinIO Console (http://localhost:9001)
2. 进入 Buckets -> edusmart-resources
3. 设置 Access Policy 为 `public` 或 `readwrite`

### 5. 前端API请求失败

**错误信息：**
```
Network Error / CORS Error
```

**解决方案：**
```bash
# 检查后端是否启动
curl http://localhost:8080/api/health

# 检查Vite代理配置
# frontend/vite.config.ts
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```

### 6. JWT Token过期

**错误信息：**
```
401 Unauthorized
```

**解决方案：**
1. 重新登录获取新Token
2. 检查JWT密钥配置是否一致
3. 调整Token过期时间（默认24小时）

### 7. 文件上传失败

**错误信息：**
```
MaxUploadSizeExceededException
```

**解决方案：**
```yaml
# 增加上传限制
spring.servlet.multipart:
  max-file-size: 100MB
  max-request-size: 100MB
```

---

## 性能优化建议

### 1. MySQL优化

```sql
-- 调整连接池大小
spring.datasource.hikari.maximum-pool-size: 50
spring.datasource.hikari.minimum-idle: 10

-- 启用查询缓存
SET GLOBAL query_cache_size = 1048576;
```

### 2. Redis优化

```bash
# 调整最大内存
redis-cli CONFIG SET maxmemory 256mb
redis-cli CONFIG SET maxmemory-policy allkeys-lru
```

### 3. JVM优化

```bash
java -Xms512m -Xmx2g -XX:+UseG1GC -jar app.jar
```

---

## 监控与日志

### 1. 应用日志

```bash
# 查看后端日志
tail -f logs/edusmart.log

# Docker日志
docker-compose logs -f backend
```

### 2. 健康检查

```bash
# 后端健康检查
curl http://localhost:8080/api/health

# 返回示例
{
  "status": "UP",
  "timestamp": 1715068800000
}
```

---

## 备份与恢复

### 1. MySQL备份

```bash
# 备份
mysqldump -u root -p edusmart > backup_$(date +%Y%m%d).sql

# 恢复
mysql -u root -p edusmart < backup_20250509.sql
```

### 2. PostgreSQL备份

```bash
# 备份
pg_dump -U postgres edusmart_vector > pgvector_backup_$(date +%Y%m%d).sql

# 恢复
psql -U postgres edusmart_vector < pgvector_backup_20250509.sql
```

### 3. MinIO备份

```bash
# 使用mc客户端
mc alias set local http://localhost:9000 minioadmin minioadmin
mc mirror local/edusmart-resources ./backup/minio/
```

---

## 更新与升级

### 1. 后端更新

```bash
# 拉取最新代码
git pull

# 重新构建
mvn clean package -DskipTests

# 重启服务
docker-compose restart backend
```

### 2. 前端更新

```bash
# 拉取最新代码
git pull

# 重新构建
npm run build

# 重启Nginx
docker-compose restart frontend
```

---

## 技术支持

如有问题，请检查：
1. 日志文件：`logs/edusmart.log`
2. Swagger文档：http://localhost:8080/swagger-ui.html
3. 项目文档：`项目设计规范文档.md`

---

*文档版本: v1.0*
*更新时间: 2026-05-09*

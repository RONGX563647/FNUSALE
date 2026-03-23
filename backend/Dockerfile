# 校园二手交易平台 - 微服务通用 Dockerfile
# 多阶段构建，优化镜像大小

# 构建阶段
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# 复制 pom 文件，利用 Docker 缓存
COPY pom.xml .
COPY fnusale-common/pom.xml fnusale-common/
COPY fnusale-gateway/pom.xml fnusale-gateway/
COPY fnusale-user/pom.xml fnusale-user/
COPY fnusale-product/pom.xml fnusale-product/
COPY fnusale-im/pom.xml fnusale-im/
COPY fnusale-ai-guide/pom.xml fnusale-ai-guide/
COPY fnusale-marketing/pom.xml fnusale-marketing/
COPY fnusale-trade/pom.xml fnusale-trade/
COPY fnusale-admin/pom.xml fnusale-admin/
COPY fnusale-agent/pom.xml fnusale-agent/

# 下载依赖（利用缓存）
RUN mvn dependency:go-offline -B

# 复制源代码
COPY . .

# 构建应用（跳过测试）
ARG SERVICE_NAME
RUN mvn clean package -DskipTests -Dmaven.test.skip=true -pl ${SERVICE_NAME} -am

# 运行阶段
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="fnusale-team"
LABEL version="1.0.0"

# 安装必要工具
RUN apk add --no-cache curl tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

# 创建非 root 用户
RUN addgroup -S fnusale && adduser -S fnusale -G fnusale

WORKDIR /app

# 从构建阶段复制 jar 文件
ARG SERVICE_NAME
COPY --from=builder /build/${SERVICE_NAME}/target/*.jar app.jar

# 设置权限
RUN chown -R fnusale:fnusale /app

USER fnusale

# JVM 参数
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:+HeapDumpOnOutOfMemoryError"

# 暴露端口（由子镜像或运行时指定）
ARG SERVICE_PORT
EXPOSE ${SERVICE_PORT}

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:${SERVICE_PORT}/actuator/health || exit 1

# 启动命令
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
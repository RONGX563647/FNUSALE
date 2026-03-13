# 校园二手交易平台 - 后端服务

基于 Java 21 + Spring Cloud Alibaba 的校园二手交易平台后端服务。

## 技术栈

| 类型          | 技术                 | 版本   |
| ------------- | -------------------- | ------ |
| 基础语言      | Java                 | 21     |
| 开发框架      | Spring Boot          | 3.2+   |
| 微服务        | Spring Cloud Alibaba | 2023.x |
| 注册/配置中心 | Nacos                | 2.3.0  |
| 限流/熔断     | Sentinel             | 1.8.6  |
| 数据库        | MySQL                | 8.0    |
| 缓存          | Redis                | 7.0    |
| 消息队列      | RabbitMQ             | 3.12   |
| 搜索引擎      | Elasticsearch        | 8.0    |
| 对象存储      | MinIO                | latest |
| 监控          | Prometheus + Grafana | latest |

## 快速开始

### 前置条件

- Docker Desktop 已安装
- Docker Compose 已安装
- 内存至少 8GB（推荐 16GB）

### 1. 环境配置

```bash
# 进入 Docker 配置目录
cd backend/docker

# 复制环境变量模板
cp .env.example .env

# 修改 .env 文件中的配置（可选）
vim .env
```

### 2. 启动所有服务

```bash
# 启动所有中间件
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看服务日志
docker-compose logs -f
```

### 3. 验证服务

| 服务        | 地址                              | 用户名/密码             |
| ----------- | --------------------------------- | ----------------------- |
| Nacos       | http://localhost:8848/nacos       | nacos/nacos             |
| Sentinel    | http://localhost:8858             | sentinel/sentinel123456 |
| MinIO       | http://localhost:9001             | admin/minio123456       |
| Grafana     | http://localhost:3000             | admin/grafana123456     |
| RabbitMQ    | http://localhost:15672            | admin/rabbitmq123456    |
| Prometheus  | http://localhost:9090             | -                       |
| Elasticsearch | http://localhost:9200           | -                       |

### 4. 停止服务

```bash
# 停止所有服务
docker-compose down

# 停止并删除数据卷（清除所有数据）
docker-compose down -v
```

## 服务端口说明

| 服务          | 端口  | 说明           |
| ------------- | ----- | -------------- |
| MySQL         | 3307  | 数据库         |
| Redis         | 6379  | 缓存           |
| Nacos         | 8848  | 注册/配置中心  |
| Sentinel      | 8858  | 限流控制台     |
| RabbitMQ      | 5672  | 消息队列       |
| RabbitMQ Mgmt | 15672 | 管理界面       |
| Elasticsearch | 9200  | 搜索引擎       |
| MinIO API     | 9000  | API端口        |
| MinIO Console | 9001  | 管理界面       |
| Prometheus    | 9090  | 监控采集       |
| Grafana       | 3000  | 监控可视化     |

## 项目结构

```
backend/
├── docker/                     # Docker 配置
│   ├── docker-compose.yml      # 服务编排
│   ├── .env.example            # 环境变量模板
│   ├── mysql/init/             # MySQL 初始化脚本
│   └── prometheus/             # Prometheus 配置
├── services/                   # 微服务模块
├── common/                     # 公共模块
└── pom.xml                     # Maven 父 POM
```

## 常见问题

### 1. Elasticsearch 启动失败

ES 对内存要求较高，如果启动失败，可以调整 `docker-compose.yml` 中的内存配置：

```yaml
environment:
  - "ES_JAVA_OPTS=-Xms256m -Xmx256m"
```

### 2. Nacos 连接 MySQL 失败

确保 MySQL 服务已完全启动，Nacos 配置了正确的数据库连接信息。

### 3. 端口冲突

如果本地端口被占用，可以修改 `.env` 文件中的端口配置。

### 4. Apple Silicon (M1/M2) 兼容性

Nacos 和 Sentinel 镜像基于 linux/amd64 平台，在 Apple Silicon Mac 上会通过 Rosetta 模拟运行，性能略有下降但不影响开发使用。

## 安全提示

- 生产环境请修改 `.env` 中的所有默认密码
- 不要将 `.env` 文件提交到版本控制
- 建议配置防火墙规则，限制外部访问

## License

MIT
# Docker 配置说明

## 概述

本目录包含校园二手交易平台的所有Docker配置文件，用于快速搭建开发和测试环境。

## 技术栈

| 组件 | 版本 | 用途 | 端口 |
|------|------|------|------|
| MySQL | 8.0 | 主数据库 | 3306 |
| Redis | 7.0 | 缓存/秒杀库存/IM会话 | 6379 |
| Elasticsearch | 8.0 | 商品搜索 | 9200 |
| MinIO | latest | 对象存储 | 9000/9001 |
| Nacos | 2.2.3 | 注册/配置中心 | 8848 |
| Sentinel | 1.8.6 | 限流/熔断控制台 | 8858 |
| RocketMQ | 5.1.0 | 消息队列 | 9876/10911 |
| RocketMQ Dashboard | latest | 消息队列管理 | 8180 |
| Prometheus | latest | 监控数据采集 | 9090 |
| Grafana | latest | 监控可视化 | 3000 |

## 快速开始

### 1. 环境准备

确保已安装以下软件：
- Docker Desktop (推荐最新版本)
- Docker Compose (通常随Docker Desktop安装)

### 2. 配置环境变量

```bash
# 复制环境变量模板
cp .env.example .env

# 编辑环境变量（可选，使用默认值即可）
vim .env
```

### 3. 启动服务

```bash
# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f [服务名]
```

### 4. 停止服务

```bash
# 停止所有服务
docker-compose down

# 停止并删除数据卷（清空所有数据）
docker-compose down -v
```

## 服务访问地址

启动成功后，可以通过以下地址访问各服务：

### 数据存储层
- **MySQL**: `localhost:3306`
  - 用户名: `fnusale` (或配置的MYSQL_USER)
  - 密码: `fnusale123456` (或配置的MYSQL_PASSWORD)
  - 数据库: `fnusale`

- **Redis**: `localhost:6379`
  - 密码: `redis123456` (或配置的REDIS_PASSWORD)

- **Elasticsearch**: `http://localhost:9200`
  - 无需认证

- **MinIO**: 
  - API: `http://localhost:9000`
  - 控制台: `http://localhost:9001`
  - 用户名: `admin` (或配置的MINIO_USER)
  - 密码: `minio123456` (或配置的MINIO_PASSWORD)

### 微服务治理层
- **Nacos**: `http://localhost:8848/nacos`
  - 用户名: `nacos`
  - 密码: `nacos`

- **Sentinel**: `http://localhost:8858`
  - 用户名: `sentinel` (或配置的SENTINEL_USER)
  - 密码: `sentinel123456` (或配置的SENTINEL_PASSWORD)

### 消息队列
- **RocketMQ NameServer**: `localhost:9876`
- **RocketMQ Broker**: `localhost:10911`
- **RocketMQ Dashboard**: `http://localhost:8180`

### 监控层
- **Prometheus**: `http://localhost:9090`
- **Grafana**: `http://localhost:3000`
  - 用户名: `admin` (或配置的GRAFANA_USER)
  - 密码: `grafana123456` (或配置的GRAFANA_PASSWORD)

## 资源配置

根据校园服务器规模，各服务的资源限制如下：

| 服务 | CPU限制 | 内存限制 | CPU预留 | 内存预留 |
|------|---------|----------|---------|----------|
| MySQL | 1.0 | 512M | 0.5 | 256M |
| Redis | 0.5 | 512M | 0.25 | 256M |
| Elasticsearch | 1.0 | 1G | 0.5 | 512M |
| MinIO | 0.5 | 512M | 0.25 | 256M |
| Nacos | 1.0 | 768M | 0.5 | 512M |
| Sentinel | 0.5 | 512M | 0.25 | 256M |
| RocketMQ NameServer | 0.5 | 512M | 0.25 | 256M |
| RocketMQ Broker | 1.0 | 768M | 0.5 | 512M |
| RocketMQ Dashboard | 0.25 | 256M | 0.1 | 128M |
| Prometheus | 0.5 | 512M | 0.25 | 256M |
| Grafana | 0.25 | 256M | 0.1 | 128M |

**总资源需求**：
- CPU: 约 6 核
- 内存: 约 6 GB

## 常见问题

### 1. MySQL启动失败

**问题**: MySQL容器启动后立即退出

**解决方案**:
```bash
# 检查日志
docker-compose logs mysql

# 可能是数据卷权限问题，尝试删除旧数据
docker-compose down -v
docker-compose up -d
```

### 2. Nacos连接MySQL失败

**问题**: Nacos无法连接到MySQL

**解决方案**:
```bash
# 确保MySQL已完全启动
docker-compose ps mysql

# 检查MySQL健康状态
docker inspect fnusale-mysql | grep -A 10 Health

# 重启Nacos
docker-compose restart nacos
```

### 3. Elasticsearch启动失败

**问题**: ES启动报错 `max virtual memory areas vm.max_map_count [65530] is too low`

**解决方案**:
```bash
# macOS/Linux
sudo sysctl -w vm.max_map_count=262144

# 永久生效（Linux）
echo "vm.max_map_count=262144" >> /etc/sysctl.conf
sudo sysctl -p
```

### 4. RocketMQ Broker无法注册到NameServer

**问题**: Broker无法连接到NameServer

**解决方案**:
```bash
# 检查broker.conf中的brokerIP1配置
# macOS使用host.docker.internal
# Linux可能需要使用实际IP地址

# 查看Broker日志
docker-compose logs rocketmq-broker
```

### 5. 内存不足

**问题**: 服务器内存不足，部分服务无法启动

**解决方案**:
```bash
# 修改docker-compose.yml中的资源限制
# 或只启动必要的服务
docker-compose up -d mysql redis nacos
```

## 数据持久化

所有数据存储在Docker命名卷中，可通过以下命令查看：

```bash
# 查看所有卷
docker volume ls | grep fnusale

# 查看卷详情
docker volume inspect fnusale-mysql-data

# 备份数据
docker run --rm -v fnusale-mysql-data:/data -v $(pwd):/backup alpine tar czf /backup/mysql-backup.tar.gz /data

# 恢复数据
docker run --rm -v fnusale-mysql-data:/data -v $(pwd):/backup alpine tar xzf /backup/mysql-backup.tar.gz -C /
```

## 生产环境建议

1. **修改默认密码**: 所有服务的默认密码都需要修改
2. **启用安全认证**: 
   - Elasticsearch启用X-Pack安全
   - Redis启用密码认证
   - MySQL启用SSL连接
3. **配置备份策略**: 定期备份MySQL和Redis数据
4. **监控告警**: 配置Prometheus告警规则
5. **日志管理**: 配置日志轮转和清理策略
6. **网络隔离**: 使用Docker网络隔离不同服务

## 目录结构

```
docker/
├── .env.example          # 环境变量模板
├── docker-compose.yml    # Docker Compose配置
├── README.md             # 本说明文档
├── mysql/
│   └── init/            # MySQL初始化脚本
│       ├── init.sql
│       ├── t_local_message.sql
│       └── test_marketing_data.sql
├── prometheus/
│   └── prometheus.yml   # Prometheus配置
└── rocketmq/
    └── broker.conf      # RocketMQ Broker配置
```

## 更新日志

### 2026-03-19
- 删除冗余的RabbitMQ配置
- 新增RocketMQ Dashboard管理控制台
- 添加资源限制配置
- 完善注释说明
- 优化MySQL、Redis、Elasticsearch配置参数
- 更新Prometheus监控配置
- 创建配置说明文档

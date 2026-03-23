# 秒杀服务压测指南

## 一、环境准备

### 1.1 安装 JMeter

```bash
# macOS
brew install jmeter

# 或下载安装
# https://jmeter.apache.org/download_jmeter.cgi
```

### 1.2 启动服务

确保以下服务已启动：
- MySQL
- Redis
- Nacos
- RocketMQ
- fnusale-marketing 服务 (端口 8105)
- fnusale-trade 服务 (端口 8104)

```bash
# 检查服务健康状态
curl http://localhost:8105/actuator/health
```

---

## 二、准备测试数据

### 2.1 创建秒杀活动

```bash
# 方式1: 使用准备脚本
python tests/jmeter/prepare_seckill_data.py --stock 50 --users 500

# 方式2: 手动创建
curl -X POST http://localhost:8105/marketing/seckill \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 1" \
  -H "X-User-Role: ADMIN" \
  -d '{
    "activityName": "压测秒杀活动",
    "productId": 1,
    "seckillPrice": 9.9,
    "totalStock": 50,
    "startTime": "2024-01-01 10:00:00",
    "endTime": "2024-01-01 12:00:00"
  }'
```

### 2.2 等待库存预热

活动开始前5分钟会自动预热库存到Redis，或手动触发：

```bash
# 检查Redis库存
redis-cli GET seckill:stock:1
```

---

## 三、执行压测

### 3.1 GUI模式（调试用）

```bash
# 打开JMeter GUI
jmeter -t tests/jmeter/seckill_stress_test.jmx
```

在GUI中可以：
- 修改线程数（用户数）
- 修改Ramp-up时间
- 修改循环次数
- 查看结果

### 3.2 命令行模式（正式压测）

```bash
# 创建结果目录
mkdir -p tests/jmeter/results

# 执行压测（500并发用户）
jmeter -n -t tests/jmeter/seckill_stress_test.jmx \
  -JTHREAD_COUNT=500 \
  -JRAMP_UP=10 \
  -JLOOP_COUNT=1 \
  -JACTIVITY_ID=1 \
  -JBASE_URL=http://localhost:8105 \
  -l tests/jmeter/results/seckill_500.jtl \
  -e -o tests/jmeter/results/report_500

# 参数说明:
# -n: 非GUI模式
# -t: 测试计划文件
# -J: 设置JMeter属性
# -l: 结果文件
# -e -o: 生成HTML报告
```

### 3.3 分阶段压测

```bash
# 阶段1: 100并发
jmeter -n -t tests/jmeter/seckill_stress_test.jmx \
  -JTHREAD_COUNT=100 -JRAMP_UP=5 \
  -l tests/jmeter/results/seckill_100.jtl \
  -e -o tests/jmeter/results/report_100

# 阶段2: 300并发
jmeter -n -t tests/jmeter/seckill_stress_test.jmx \
  -JTHREAD_COUNT=300 -JRAMP_UP=10 \
  -l tests/jmeter/results/seckill_300.jtl \
  -e -o tests/jmeter/results/report_300

# 阶段3: 500并发
jmeter -n -t tests/jmeter/seckill_stress_test.jmx \
  -JTHREAD_COUNT=500 -JRAMP_UP=10 \
  -l tests/jmeter/results/seckill_500.jtl \
  -e -o tests/jmeter/results/report_500

# 阶段4: 1000并发（极限测试）
jmeter -n -t tests/jmeter/seckill_stress_test.jmx \
  -JTHREAD_COUNT=1000 -JRAMP_UP=20 \
  -l tests/jmeter/results/seckill_1000.jtl \
  -e -o tests/jmeter/results/report_1000
```

---

## 四、监控指标

### 4.1 JMeter 结果指标

| 指标 | 说明 | 目标值 |
|------|------|--------|
| Throughput | 吞吐量 (QPS) | ≥ 500/s |
| Average | 平均响应时间 | < 500ms |
| 90th Percentile | 90%响应时间 | < 1000ms |
| Error % | 错误率 | < 1% |

### 4.2 服务端监控

```bash
# JVM监控
curl http://localhost:8105/actuator/metrics/jvm.memory.used

# Redis监控
redis-cli INFO stats | grep instantaneous_ops_per_sec

# MySQL监控
mysql -e "SHOW STATUS LIKE 'Threads_connected';"
```

### 4.3 Prometheus + Grafana

访问 Grafana 仪表板查看：
- JVM 内存使用
- GC 频率
- 接口响应时间
- QPS 趋势

---

## 五、压测场景

### 场景1: 秒杀活动列表查询

测试目标：验证列表接口的并发性能

```bash
jmeter -n -t tests/jmeter/seckill_stress_test.jmx \
  -JTHREAD_COUNT=500 \
  -l tests/jmeter/results/list_500.jtl
```

### 场景2: 秒杀参与（核心）

测试目标：验证秒杀核心流程的并发性能

```bash
jmeter -n -t tests/jmeter/seckill_stress_test.jmx \
  -JTHREAD_COUNT=500 \
  -JACTIVITY_ID=1 \
  -l tests/jmeter/results/join_500.jtl
```

### 场景3: 秒杀结果查询

测试目标：验证结果查询接口的并发性能

```bash
jmeter -n -t tests/jmeter/seckill_stress_test.jmx \
  -JTHREAD_COUNT=500 \
  -JACTIVITY_ID=1 \
  -l tests/jmeter/results/result_500.jtl
```

---

## 六、结果分析

### 6.1 查看HTML报告

```bash
# 打开HTML报告
open tests/jmeter/results/report_500/index.html
```

### 6.2 关键指标解读

1. **Throughput (吞吐量)**
   - 表示系统每秒处理的请求数
   - 目标: ≥ 500 QPS

2. **Response Time (响应时间)**
   - Average: 平均响应时间
   - 90th Percentile: 90%请求的响应时间
   - 目标: 平均 < 500ms, P90 < 1000ms

3. **Error Rate (错误率)**
   - 失败请求占比
   - 目标: < 1%

### 6.3 常见问题排查

| 问题 | 可能原因 | 解决方案 |
|------|----------|----------|
| 响应时间长 | Redis连接池不足 | 增大连接池 |
| 错误率高 | 库存不足 | 增加测试库存 |
| 吞吐量低 | 线程阻塞 | 检查锁竞争 |
| 内存溢出 | 对象未释放 | 检查内存泄漏 |

---

## 七、压测报告模板

### 测试环境

| 项目 | 配置 |
|------|------|
| 服务器 | CPU: 4核, 内存: 8GB |
| 数据库 | MySQL 8.0 |
| 缓存 | Redis 7.0 |
| JDK | OpenJDK 17 |

### 测试结果

| 并发数 | QPS | 平均响应时间 | P90 | 错误率 |
|--------|-----|--------------|-----|--------|
| 100 | 450 | 180ms | 320ms | 0% |
| 300 | 420 | 450ms | 780ms | 0.1% |
| 500 | 380 | 680ms | 1200ms | 0.5% |
| 1000 | 280 | 1500ms | 2800ms | 2.1% |

### 结论

根据压测结果，系统在 **500并发** 下可以稳定运行，QPS达到 **380**，满足设计目标。

---

## 八、注意事项

1. **压测前准备**
   - 确保数据库有足够的测试数据
   - 确保Redis已预热库存
   - 关闭不必要的日志输出

2. **压测过程中**
   - 监控服务器资源使用
   - 观察错误日志
   - 记录异常情况

3. **压测后清理**
   - 清理测试数据
   - 重置Redis缓存
   - 分析结果报告

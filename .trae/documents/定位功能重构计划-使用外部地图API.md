# 定位功能重构计划 - 使用外部地图API

## 一、现状分析

### 1.1 当前定位功能实现

项目当前使用以下方式实现定位功能：

1. **地理围栏工具类 (GeoFenceUtil)**
   - 使用射线法判断点是否在多边形内
   - 使用Haversine公式计算两点之间的距离
   - 纯后端计算，不依赖外部服务

2. **校园围栏配置 (CampusFenceConfig)**
   - 支持多边形围栏和圆形围栏
   - 配置存储在application.yml中

3. **校园自提点服务 (CampusPickPointService)**
   - 使用Redis GEO存储自提点地理位置
   - 前端传递用户经纬度，后端计算距离

### 1.2 存在的问题

1. **前端依赖重**：需要前端获取用户位置并传递经纬度
2. **精度有限**：纯数学计算无法考虑实际道路、建筑等因素
3. **功能单一**：无法提供逆地理编码、POI搜索等高级功能
4. **维护复杂**：需要手动维护Redis GEO数据

---

## 二、重构目标

### 2.1 核心目标

使用外部地图API（高德地图）替代当前的自实现定位功能，实现：

1. **IP定位**：根据用户IP自动获取大致位置
2. **逆地理编码**：将经纬度转换为详细地址
3. **地理围栏**：使用高德地图API判断是否在校园范围内
4. **附近搜索**：使用高德地图API搜索附近自提点
5. **距离计算**：使用高德地图API计算实际距离（考虑道路）

### 2.2 技术选型

**选择高德地图API**，原因：
1. 文档中已明确提到使用高德地图API
2. 国内定位精度高，适合校园场景
3. 提供丰富的API接口（定位、逆地理编码、路径规划等）
4. 有免费额度，适合校园项目

---

## 三、修改方案

### 3.1 新增模块

```
backend/fnusale-common/src/main/java/com/fnusale/common/
├── config/
│   └── AmapConfig.java              # 高德地图配置类
├── service/
│   └── AmapService.java             # 高德地图服务接口
│   └── impl/
│       └── AmapServiceImpl.java     # 高德地图服务实现
├── dto/
│   └── amap/
│       ├── AmapGeocodeResult.java   # 逆地理编码结果
│       ├── AmapLocationResult.java  # 定位结果
│       └── AmapDistanceResult.java  # 距离计算结果
```

### 3.2 修改模块

1. **CampusFenceConfig.java** - 简化围栏配置
2. **GeoFenceUtil.java** - 保留作为备用工具
3. **CampusPickPointService.java** - 改用高德地图API
4. **CampusPickPointController.java** - 调整接口参数

### 3.3 API接口调整

| 原接口 | 新接口 | 变更说明 |
|--------|--------|---------|
| GET /user/pick-point/nearby?longitude=&latitude=&distance= | GET /user/pick-point/nearby?ip=&distance= | 支持IP定位，可选传经纬度 |
| - | GET /user/location/ip | 新增：IP定位接口 |
| - | GET /user/location/geocode | 新增：逆地理编码接口 |

---

## 四、详细设计

### 4.1 高德地图API集成

#### 4.1.1 配置类

```java
@Data
@Component
@ConfigurationProperties(prefix = "amap")
public class AmapConfig {
    private String key;           // 高德地图API Key
    private String baseUrl;       // API基础URL
    private Integer connectTimeout = 5000;
    private Integer readTimeout = 10000;
}
```

#### 4.1.2 服务接口

```java
public interface AmapService {
    /**
     * IP定位
     * @param ip 用户IP地址
     * @return 定位结果（经纬度+省份城市）
     */
    AmapLocationResult locateByIp(String ip);
    
    /**
     * 逆地理编码
     * @param longitude 经度
     * @param latitude 纬度
     * @return 详细地址信息
     */
    AmapGeocodeResult reverseGeocode(String longitude, String latitude);
    
    /**
     * 判断是否在多边形区域内
     * @param longitude 经度
     * @param latitude 纬度
     * @param polygon 多边形顶点坐标
     * @return 是否在区域内
     */
    boolean isInPolygon(String longitude, String latitude, String polygon);
    
    /**
     * 计算两点间距离（直线距离）
     * @param origins 起点坐标（可多个）
     * @param destination 终点坐标
     * @return 距离结果列表
     */
    List<AmapDistanceResult> calculateDistance(String origins, String destination);
}
```

### 4.2 定位认证流程（修改后）

```
┌─────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│  开始   │───▶│ 获取用户IP  │───▶│ 调用高德地图 │───▶│ 判断是否在  │
└─────────┘    │ 或前端传经纬度│    │ IP定位API   │    │ 校园围栏内  │
               └─────────────┘    └─────────────┘    └──────┬──────┘
                                                           │
                    ┌──────────────────────────────────────┤
                    │                                      │
                    ▼ 在围栏内                             ▼ 不在围栏内
            ┌───────────────┐                      ┌───────────────┐
            │ 允许发布商品  │                      │ 提示：请在校园│
            │ 允许参与交易  │                      │ 内进行交易    │
            └───────────────┘                      └───────────────┘
```

### 4.3 附近自提点查询（修改后）

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│ 用户请求    │───▶│ 获取用户位置 │───▶│ 查询数据库  │───▶│ 调用高德地图 │
│ (IP或经纬度)│    │ (IP定位或前端)│    │ 获取自提点  │    │ 计算距离    │
└─────────────┘    └─────────────┘    └─────────────┘    └──────┬──────┘
                                                           │
                                                           ▼
                                                   ┌───────────────┐
                                                   │ 按距离排序    │
                                                   │ 返回结果      │
                                                   └───────────────┘
```

---

## 五、实施步骤

### 阶段一：基础设施搭建（预计1天）

| 步骤 | 任务 | 说明 |
|------|------|------|
| 1.1 | 添加高德地图SDK依赖 | 在fnusale-common的pom.xml中添加 |
| 1.2 | 创建配置类 | AmapConfig.java |
| 1.3 | 创建DTO类 | 定位结果、逆地理编码结果等 |
| 1.4 | 实现AmapService | 封装高德地图API调用 |
| 1.5 | 编写单元测试 | 测试API调用是否正常 |

### 阶段二：定位功能重构（预计1天）

| 步骤 | 任务 | 说明 |
|------|------|------|
| 2.1 | 新增IP定位接口 | GET /user/location/ip |
| 2.2 | 新增逆地理编码接口 | GET /user/location/geocode |
| 2.3 | 修改围栏校验逻辑 | 改用高德地图API |
| 2.4 | 保留GeoFenceUtil作为备用 | 添加降级逻辑 |

### 阶段三：自提点服务重构（预计1天）

| 步骤 | 任务 | 说明 |
|------|------|------|
| 3.1 | 修改附近自提点查询 | 支持IP定位，改用高德计算距离 |
| 3.2 | 移除Redis GEO依赖 | 简化代码 |
| 3.3 | 添加缓存机制 | 缓存定位结果，减少API调用 |
| 3.4 | 更新API文档 | 同步修改接口文档 |

### 阶段四：测试与优化（预计0.5天）

| 步骤 | 任务 | 说明 |
|------|------|------|
| 4.1 | 编写API测试脚本 | 使用Python requests测试 |
| 4.2 | 前后端联调测试 | 验证定位功能正常 |
| 4.3 | 性能测试 | 测试API响应时间 |
| 4.4 | 更新progress.md | 记录完成进度 |

---

## 六、配置说明

### 6.1 application.yml 配置

```yaml
# 高德地图配置
amap:
  key: ${AMAP_KEY:your-amap-key}
  base-url: https://restapi.amap.com/v3
  connect-timeout: 5000
  read-timeout: 10000

# 校园围栏配置（简化）
campus:
  fence:
    # 校园中心点
    center: 116.407526,39.904030
    # 围栏半径（米）
    radius: 1500
    # 是否启用围栏验证
    enabled: true
```

### 6.2 环境变量

需要在部署环境中配置：
- `AMAP_KEY`: 高德地图API Key

---

## 七、API调用示例

### 7.1 IP定位

**请求：**
```
GET /user/location/ip
X-Forwarded-For: 116.407526
```

**响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "longitude": "116.407526",
    "latitude": "39.904030",
    "province": "北京市",
    "city": "北京市",
    "district": "海淀区",
    "address": "北京市海淀区"
  }
}
```

### 7.2 附近自提点

**请求：**
```
GET /user/pick-point/nearby?distance=1000
X-Forwarded-For: 116.407526
```

**响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "pickPointName": "1号宿舍楼楼下",
      "campusArea": "东校区",
      "detailAddress": "1号宿舍楼一楼大厅",
      "longitude": "116.407526",
      "latitude": "39.904030",
      "distance": 150,
      "enableStatus": true
    }
  ]
}
```

---

## 八、风险与应对

| 风险 | 影响 | 应对措施 |
|------|------|---------|
| 高德API调用失败 | 定位功能不可用 | 降级到GeoFenceUtil本地计算 |
| API调用超限 | 服务中断 | 添加缓存，控制调用频率 |
| IP定位精度低 | 位置不准确 | 优先使用前端GPS定位，IP定位作为备用 |
| API Key泄露 | 安全风险 | 使用环境变量存储，定期轮换 |

---

## 九、验收标准

1. ✅ IP定位接口正常工作
2. ✅ 逆地理编码接口正常工作
3. ✅ 围栏校验功能正常
4. ✅ 附近自提点查询功能正常
5. ✅ API测试脚本通过
6. ✅ 前后端联调测试通过
7. ✅ 文档更新完成

---

**计划制定时间**: 2026-03-19
**预计完成时间**: 3.5天

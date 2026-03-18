<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { logApi } from '@/api'
import { formatDateTime } from '@/utils/format'
import { Download } from '@element-plus/icons-vue'
import type { SystemLog, LogQueryParams, LogModule, OperateType } from '@/types'

// 数据
const tableData = ref<SystemLog[]>([])
const total = ref(0)
const loading = ref(false)

// 查询参数
const queryParams = reactive<LogQueryParams>({
  pageNum: 1,
  pageSize: 10,
  moduleName: undefined,
  operateType: undefined,
  startDate: undefined,
  endDate: undefined
})

// 日期范围
const dateRange = ref<[string, string] | null>(null)

// 加载数据
async function loadData() {
  loading.value = true
  try {
    if (dateRange.value) {
      queryParams.startDate = dateRange.value[0]
      queryParams.endDate = dateRange.value[1]
    } else {
      queryParams.startDate = undefined
      queryParams.endDate = undefined
    }
    const res = await logApi.getPage(queryParams)
    tableData.value = res.data.list
    total.value = res.data.total
  } catch (error) {
    console.error('Load logs failed:', error)
    // Mock 数据
    tableData.value = [
      {
        logId: 1,
        operateUserId: 1,
        operateUsername: 'admin',
        moduleName: 'PRODUCT',
        operateType: 'UPDATE',
        operateContent: '审核通过商品ID:1001',
        ipAddress: '192.168.1.100',
        deviceInfo: 'Chrome 120.0 / Windows 10',
        logType: 'OPERATE',
        createTime: '2024-01-15 10:30:00'
      },
      {
        logId: 2,
        operateUserId: 1,
        operateUsername: 'admin',
        moduleName: 'USER',
        operateType: 'UPDATE',
        operateContent: '封禁用户ID:1002, 原因: 发布违规商品',
        ipAddress: '192.168.1.100',
        deviceInfo: 'Chrome 120.0 / Windows 10',
        logType: 'OPERATE',
        createTime: '2024-01-15 10:25:00'
      },
      {
        logId: 3,
        operateUserId: 1,
        operateUsername: 'admin',
        moduleName: 'SYSTEM',
        operateType: 'UPDATE',
        operateContent: '更新系统配置: seckill_qps_limit = 500',
        ipAddress: '192.168.1.100',
        deviceInfo: 'Chrome 120.0 / Windows 10',
        logType: 'OPERATE',
        createTime: '2024-01-15 10:20:00'
      },
      {
        logId: 4,
        operateUserId: 1,
        operateUsername: 'admin',
        moduleName: 'ORDER',
        operateType: 'UPDATE',
        operateContent: '处理纠纷ID:1, 结果: 买家胜诉',
        ipAddress: '192.168.1.100',
        deviceInfo: 'Chrome 120.0 / Windows 10',
        logType: 'OPERATE',
        createTime: '2024-01-15 10:15:00'
      }
    ]
    total.value = 4
  } finally {
    loading.value = false
  }
}

// 搜索
function handleSearch() {
  queryParams.pageNum = 1
  loadData()
}

// 重置
function handleReset() {
  queryParams.moduleName = undefined
  queryParams.operateType = undefined
  dateRange.value = null
  queryParams.pageNum = 1
  loadData()
}

// 分页变化
function handlePageChange(page: number) {
  queryParams.pageNum = page
  loadData()
}

function handleSizeChange(size: number) {
  queryParams.pageSize = size
  queryParams.pageNum = 1
  loadData()
}

// 导出日志
async function handleExport() {
  try {
    const startDate = dateRange.value?.[0]
    const endDate = dateRange.value?.[1]
    const res = await logApi.export(startDate, endDate)
    window.open(res.data, '_blank')
  } catch (error) {
    console.error('Export failed:', error)
  }
}

// 日志模块文本
function moduleNameText(module: LogModule): string {
  const map: Record<LogModule, string> = {
    USER: '用户模块',
    PRODUCT: '商品模块',
    ORDER: '订单模块',
    MARKETING: '营销模块',
    SYSTEM: '系统模块',
    DISPUTE: '纠纷模块'
  }
  return map[module] || module
}

// 操作类型文本
function operateTypeText(type: OperateType): string {
  const map: Record<OperateType, string> = {
    ADD: '新增',
    UPDATE: '更新',
    DELETE: '删除',
    QUERY: '查询'
  }
  return map[type] || type
}

// 操作类型标签类型
function operateTypeTagType(type: OperateType): 'primary' | 'success' | 'warning' | 'danger' | 'info' {
  const map: Record<OperateType, 'primary' | 'success' | 'warning' | 'danger' | 'info'> = {
    ADD: 'success',
    UPDATE: 'warning',
    DELETE: 'danger',
    QUERY: 'info'
  }
  return map[type] || 'primary'
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="log-list">
    <el-card>
      <!-- 搜索栏 -->
      <template #header>
        <div class="card-header">
          <el-form :inline="true" :model="queryParams" @submit.prevent="handleSearch">
            <el-form-item label="操作模块">
              <el-select v-model="queryParams.moduleName" placeholder="全部" clearable>
                <el-option label="用户模块" value="USER" />
                <el-option label="商品模块" value="PRODUCT" />
                <el-option label="订单模块" value="ORDER" />
                <el-option label="营销模块" value="MARKETING" />
                <el-option label="系统模块" value="SYSTEM" />
                <el-option label="纠纷模块" value="DISPUTE" />
              </el-select>
            </el-form-item>
            <el-form-item label="操作类型">
              <el-select v-model="queryParams.operateType" placeholder="全部" clearable>
                <el-option label="新增" value="ADD" />
                <el-option label="更新" value="UPDATE" />
                <el-option label="删除" value="DELETE" />
                <el-option label="查询" value="QUERY" />
              </el-select>
            </el-form-item>
            <el-form-item label="日期范围">
              <el-date-picker
                v-model="dateRange"
                type="daterange"
                range-separator="-"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">搜索</el-button>
              <el-button @click="handleReset">重置</el-button>
              <el-button :icon="Download" @click="handleExport">导出</el-button>
            </el-form-item>
          </el-form>
        </div>
      </template>

      <!-- 表格 -->
      <el-table v-loading="loading" :data="tableData" style="width: 100%">
        <el-table-column prop="logId" label="ID" width="80" />
        <el-table-column prop="operateUsername" label="操作人" width="100" />
        <el-table-column label="操作模块" width="100">
          <template #default="{ row }">
            {{ moduleNameText(row.moduleName) }}
          </template>
        </el-table-column>
        <el-table-column label="操作类型" width="80">
          <template #default="{ row }">
            <el-tag :type="operateTypeTagType(row.operateType)" size="small">
              {{ operateTypeText(row.operateType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operateContent" label="操作内容" min-width="250" show-overflow-tooltip />
        <el-table-column prop="ipAddress" label="IP地址" width="140" />
        <el-table-column prop="deviceInfo" label="设备信息" width="200" show-overflow-tooltip />
        <el-table-column label="操作时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<style lang="scss" scoped>
.log-list {
  .card-header {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
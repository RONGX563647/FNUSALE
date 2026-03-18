<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { statisticsApi, auditApi } from '@/api'
import { formatMoney, formatNumber } from '@/utils/format'
import type { TodayStatistics, TrendData, TrendSeries, CategoryStatistics, AuditStatistics } from '@/types'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, PieChart, BarChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import { User, ShoppingCart, Goods, Money, DocumentChecked, Warning } from '@element-plus/icons-vue'

// 注册 ECharts 组件
use([
  CanvasRenderer,
  LineChart,
  PieChart,
  BarChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
])

const router = useRouter()

// 数据
const todayStats = ref<TodayStatistics | null>(null)
const auditStats = ref<AuditStatistics | null>(null)
const productTrend = ref<TrendData | null>(null)
const orderTrend = ref<TrendData | null>(null)
const categoryStats = ref<CategoryStatistics[]>([])
const loading = ref(false)

// 统计卡片
const statCards = computed(() => [
  {
    title: '新增用户',
    value: todayStats.value?.newUserCount || 0,
    icon: User,
    color: '#409eff',
    type: 'number'
  },
  {
    title: '活跃用户',
    value: todayStats.value?.activeUserCount || 0,
    icon: User,
    color: '#67c23a',
    type: 'number'
  },
  {
    title: '商品发布',
    value: todayStats.value?.productPublishCount || 0,
    icon: Goods,
    color: '#e6a23c',
    type: 'number'
  },
  {
    title: '成交订单',
    value: todayStats.value?.orderSuccessCount || 0,
    icon: ShoppingCart,
    color: '#f56c6c',
    type: 'number'
  },
  {
    title: '成交金额',
    value: todayStats.value?.orderSuccessAmount || 0,
    icon: Money,
    color: '#909399',
    type: 'money'
  },
  {
    title: '待审核商品',
    value: todayStats.value?.pendingAuditCount || 0,
    icon: DocumentChecked,
    color: '#409eff',
    type: 'number',
    clickable: true,
    path: '/audit'
  },
  {
    title: '待认证审核',
    value: todayStats.value?.pendingAuthCount || 0,
    icon: DocumentChecked,
    color: '#e6a23c',
    type: 'number',
    clickable: true,
    path: '/user/auth'
  },
  {
    title: '待处理纠纷',
    value: todayStats.value?.pendingDisputeCount || 0,
    icon: Warning,
    color: '#f56c6c',
    type: 'number',
    clickable: true,
    path: '/dispute'
  }
])

// 商品趋势图配置
const productTrendOption = computed(() => ({
  title: { text: '商品发布趋势', left: 'center' },
  tooltip: { trigger: 'axis' },
  legend: { data: productTrend.value?.series.map((s: TrendSeries) => s.name) || [], bottom: 0 },
  grid: { left: '3%', right: '4%', bottom: '15%', containLabel: true },
  xAxis: { type: 'category', data: productTrend.value?.xAxis || [] },
  yAxis: { type: 'value' },
  series: (productTrend.value?.series || []).map((s: TrendSeries) => ({
    name: s.name,
    type: 'line',
    smooth: true,
    data: s.data
  }))
}))

// 成交趋势图配置
const orderTrendOption = computed(() => ({
  title: { text: '成交趋势', left: 'center' },
  tooltip: { trigger: 'axis' },
  legend: { data: orderTrend.value?.series.map((s: TrendSeries) => s.name) || [], bottom: 0 },
  grid: { left: '3%', right: '4%', bottom: '15%', containLabel: true },
  xAxis: { type: 'category', data: orderTrend.value?.xAxis || [] },
  yAxis: { type: 'value' },
  series: (orderTrend.value?.series || []).map((s: TrendSeries) => ({
    name: s.name,
    type: 'line',
    smooth: true,
    data: s.data
  }))
}))

// 品类分布图配置
const categoryOption = computed(() => ({
  title: { text: '热门品类分布', left: 'center' },
  tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
  legend: { orient: 'vertical', left: 'left', top: 'middle' },
  series: [{
    type: 'pie',
    radius: ['40%', '70%'],
    center: ['60%', '50%'],
    data: categoryStats.value.map((c: CategoryStatistics) => ({ name: c.categoryName, value: c.productCount })),
    label: { show: false }
  }]
}))

// 加载数据
async function loadData() {
  loading.value = true
  try {
    // 并行请求
    const [todayRes, auditRes, productRes, orderRes, categoryRes] = await Promise.all([
      statisticsApi.getToday(),
      auditApi.getStatistics(),
      statisticsApi.getProductTrend(7),
      statisticsApi.getOrderTrend(7),
      statisticsApi.getHotCategory()
    ])

    todayStats.value = todayRes.data
    auditStats.value = auditRes.data
    productTrend.value = productRes.data
    orderTrend.value = orderRes.data
    categoryStats.value = categoryRes.data
  } catch (error) {
    console.error('Load dashboard data failed:', error)
    // 使用 mock 数据
    todayStats.value = {
      newUserCount: 50,
      activeUserCount: 200,
      productPublishCount: 80,
      orderSuccessCount: 120,
      orderSuccessAmount: 3500,
      pendingAuditCount: 15,
      pendingAuthCount: 8,
      pendingDisputeCount: 3
    }
    productTrend.value = {
      xAxis: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
      series: [
        { name: '发布数量', data: [65, 72, 80, 75, 90, 85, 78] },
        { name: '审核通过', data: [60, 68, 75, 70, 85, 80, 72] }
      ]
    }
    orderTrend.value = {
      xAxis: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
      series: [
        { name: '订单数', data: [100, 110, 120, 105, 130, 125, 115] },
        { name: '成交金额', data: [2500, 2800, 3200, 2700, 3500, 3300, 3000] }
      ]
    }
    categoryStats.value = [
      { categoryId: 1, categoryName: '教材', productCount: 500, orderCount: 300, orderAmount: 6000, percentage: 25.5 },
      { categoryId: 2, categoryName: '电子产品', productCount: 300, orderCount: 150, orderAmount: 15000, percentage: 15.3 },
      { categoryId: 3, categoryName: '生活用品', productCount: 250, orderCount: 180, orderAmount: 3000, percentage: 12.8 },
      { categoryId: 4, categoryName: '服装', productCount: 200, orderCount: 120, orderAmount: 2500, percentage: 10.2 }
    ]
  } finally {
    loading.value = false
  }
}

function handleCardClick(card: typeof statCards.value[0]) {
  if (card.clickable && card.path) {
    router.push(card.path)
  }
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="dashboard" v-loading="loading">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col v-for="card in statCards" :key="card.title" :xs="12" :sm="8" :md="6">
        <el-card
          class="stat-card"
          :class="{ clickable: card.clickable }"
          shadow="hover"
          @click="handleCardClick(card)"
        >
          <div class="stat-icon" :style="{ backgroundColor: card.color }">
            <el-icon :size="24">
              <component :is="card.icon" />
            </el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">
              {{ card.type === 'money' ? '¥' + formatMoney(card.value) : formatNumber(card.value) }}
            </div>
            <div class="stat-title">{{ card.title }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 趋势图表 -->
    <el-row :gutter="20" class="charts-row">
      <el-col :xs="24" :lg="12">
        <el-card>
          <v-chart :option="productTrendOption" autoresize style="height: 300px" />
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card>
          <v-chart :option="orderTrendOption" autoresize style="height: 300px" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 品类分布 -->
    <el-row :gutter="20" class="charts-row">
      <el-col :xs="24" :lg="12">
        <el-card>
          <v-chart :option="categoryOption" autoresize style="height: 300px" />
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>热门品类详情</span>
            </div>
          </template>
          <el-table :data="categoryStats" style="width: 100%">
            <el-table-column prop="categoryName" label="品类" />
            <el-table-column prop="productCount" label="商品数" />
            <el-table-column prop="orderCount" label="订单数" />
            <el-table-column label="成交金额">
              <template #default="{ row }">
                ¥{{ formatMoney(row.orderAmount) }}
              </template>
            </el-table-column>
            <el-table-column label="占比">
              <template #default="{ row }">
                {{ row.percentage }}%
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style lang="scss" scoped>
.dashboard {
  .stat-cards {
    margin-bottom: 20px;
  }

  .stat-card {
    display: flex;
    align-items: center;
    padding: 16px;
    margin-bottom: 20px;

    &.clickable {
      cursor: pointer;

      &:hover {
        transform: translateY(-2px);
      }
    }

    .stat-icon {
      width: 48px;
      height: 48px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      margin-right: 16px;
    }

    .stat-content {
      flex: 1;

      .stat-value {
        font-size: 24px;
        font-weight: bold;
        color: #303133;
      }

      .stat-title {
        font-size: 14px;
        color: #909399;
        margin-top: 4px;
      }
    }
  }

  .charts-row {
    margin-bottom: 20px;
  }

  .card-header {
    font-size: 16px;
    font-weight: bold;
  }
}
</style>
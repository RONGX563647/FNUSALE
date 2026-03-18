<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { statisticsApi } from '@/api'
import { formatMoney } from '@/utils/format'
import type { TrendData, TrendSeries, CategoryStatistics } from '@/types'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, PieChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import { Download } from '@element-plus/icons-vue'

// 注册 ECharts 组件
use([
  CanvasRenderer,
  LineChart,
  PieChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
])

// 数据
const loading = ref(false)
const productTrend = ref<TrendData | null>(null)
const orderTrend = ref<TrendData | null>(null)
const userTrend = ref<TrendData | null>(null)
const categoryStats = ref<CategoryStatistics[]>([])

// 日期范围
const dateRange = ref(30)

// 商品趋势图配置
const productTrendOption = computed(() => ({
  title: { text: '商品发布趋势', left: 'center' },
  tooltip: { trigger: 'axis' },
  legend: { data: productTrend.value?.series.map((s: TrendSeries) => s.name) || [], bottom: 0 },
  grid: { left: '3%', right: '4%', bottom: '15%', containLabel: true },
  xAxis: { type: 'category', data: productTrend.value?.xAxis || [], axisLabel: { rotate: 45 } },
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
  xAxis: { type: 'category', data: orderTrend.value?.xAxis || [], axisLabel: { rotate: 45 } },
  yAxis: { type: 'value' },
  series: (orderTrend.value?.series || []).map((s: TrendSeries) => ({
    name: s.name,
    type: 'line',
    smooth: true,
    data: s.data
  }))
}))

// 用户增长趋势图配置
const userTrendOption = computed(() => ({
  title: { text: '用户增长趋势', left: 'center' },
  tooltip: { trigger: 'axis' },
  legend: { data: userTrend.value?.series.map((s: TrendSeries) => s.name) || [], bottom: 0 },
  grid: { left: '3%', right: '4%', bottom: '15%', containLabel: true },
  xAxis: { type: 'category', data: userTrend.value?.xAxis || [], axisLabel: { rotate: 45 } },
  yAxis: { type: 'value' },
  series: (userTrend.value?.series || []).map((s: TrendSeries) => ({
    name: s.name,
    type: 'line',
    smooth: true,
    data: s.data
  }))
}))

// 品类分布图配置
const categoryOption = computed(() => ({
  title: { text: '品类分布', left: 'center' },
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
    const [productRes, orderRes, userRes, categoryRes] = await Promise.all([
      statisticsApi.getProductTrend(dateRange.value),
      statisticsApi.getOrderTrend(dateRange.value),
      statisticsApi.getUserGrowth(dateRange.value),
      statisticsApi.getHotCategory()
    ])
    productTrend.value = productRes.data
    orderTrend.value = orderRes.data
    userTrend.value = userRes.data
    categoryStats.value = categoryRes.data
  } catch (error) {
    console.error('Load statistics failed:', error)
    // Mock 数据
    const dates = Array.from({ length: dateRange.value }, (_, i) => {
      const d = new Date()
      d.setDate(d.getDate() - (dateRange.value - i - 1))
      return `${d.getMonth() + 1}/${d.getDate()}`
    })
    productTrend.value = {
      xAxis: dates,
      series: [
        { name: '发布数量', data: Array.from({ length: dateRange.value }, () => Math.floor(Math.random() * 50) + 30) },
        { name: '审核通过', data: Array.from({ length: dateRange.value }, () => Math.floor(Math.random() * 45) + 25) }
      ]
    }
    orderTrend.value = {
      xAxis: dates,
      series: [
        { name: '订单数', data: Array.from({ length: dateRange.value }, () => Math.floor(Math.random() * 100) + 50) },
        { name: '成交金额', data: Array.from({ length: dateRange.value }, () => Math.floor(Math.random() * 5000) + 2000) }
      ]
    }
    userTrend.value = {
      xAxis: dates,
      series: [
        { name: '新增用户', data: Array.from({ length: dateRange.value }, () => Math.floor(Math.random() * 30) + 10) }
      ]
    }
    categoryStats.value = [
      { categoryId: 1, categoryName: '教材', productCount: 500, orderCount: 300, orderAmount: 6000, percentage: 25.5 },
      { categoryId: 2, categoryName: '电子产品', productCount: 300, orderCount: 150, orderAmount: 15000, percentage: 15.3 },
      { categoryId: 3, categoryName: '生活用品', productCount: 250, orderCount: 180, orderAmount: 3000, percentage: 12.8 },
      { categoryId: 4, categoryName: '服装', productCount: 200, orderCount: 120, orderAmount: 2500, percentage: 10.2 },
      { categoryId: 5, categoryName: '其他', productCount: 150, orderCount: 80, orderAmount: 1500, percentage: 7.6 }
    ]
  } finally {
    loading.value = false
  }
}

// 导出报表
async function handleExport() {
  const today = new Date()
  const startDate = new Date(today)
  startDate.setDate(startDate.getDate() - dateRange.value)

  const start = startDate.toISOString().split('T')[0]
  const end = today.toISOString().split('T')[0]

  try {
    const res = await statisticsApi.exportReport(start, end)
    window.open(res.data, '_blank')
  } catch (error) {
    console.error('Export failed:', error)
  }
}

// 日期范围变化
function handleDateRangeChange() {
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="statistics-view" v-loading="loading">
    <!-- 工具栏 -->
    <el-card class="toolbar-card">
      <div class="toolbar">
        <div class="left">
          <span>统计周期：</span>
          <el-radio-group v-model="dateRange" @change="handleDateRangeChange">
            <el-radio :value="7">近7天</el-radio>
            <el-radio :value="30">近30天</el-radio>
            <el-radio :value="90">近90天</el-radio>
          </el-radio-group>
        </div>
        <div class="right">
          <el-button type="primary" :icon="Download" @click="handleExport">
            导出报表
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 趋势图表 -->
    <el-row :gutter="20" class="charts-row">
      <el-col :xs="24" :lg="12">
        <el-card>
          <v-chart :option="productTrendOption" autoresize style="height: 350px" />
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card>
          <v-chart :option="orderTrendOption" autoresize style="height: 350px" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-row">
      <el-col :xs="24" :lg="12">
        <el-card>
          <v-chart :option="userTrendOption" autoresize style="height: 350px" />
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card>
          <v-chart :option="categoryOption" autoresize style="height: 350px" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 品类详情表 -->
    <el-card>
      <template #header>
        <span>品类统计详情</span>
      </template>
      <el-table :data="categoryStats" style="width: 100%">
        <el-table-column prop="categoryName" label="品类名称" />
        <el-table-column prop="productCount" label="商品数量" />
        <el-table-column prop="orderCount" label="订单数量" />
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
  </div>
</template>

<style lang="scss" scoped>
.statistics-view {
  .toolbar-card {
    margin-bottom: 20px;
  }

  .toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .charts-row {
    margin-bottom: 20px;
  }
}
</style>
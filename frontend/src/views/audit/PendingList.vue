<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { auditApi } from '@/api'
import { formatDateTime, formatMoney } from '@/utils/format'
import { ElMessage, ElMessageBox } from 'element-plus'
import { View, Check, Close, Delete } from '@element-plus/icons-vue'
import type { PendingProduct, AuditRecord } from '@/types'

// 数据
const tableData = ref<PendingProduct[]>([])
const total = ref(0)
const loading = ref(false)
const selectedRows = ref<PendingProduct[]>([])

// 分页
const pageNum = ref(1)
const pageSize = ref(10)

// 驳回弹窗
const rejectDialogVisible = ref(false)
const rejectReason = ref('')
const currentProductId = ref<number | null>(null)

// 审核记录弹窗
const recordsDialogVisible = ref(false)
const auditRecords = ref<AuditRecord[]>([])
const recordsLoading = ref(false)

// 强制下架弹窗
const forceOffDialogVisible = ref(false)
const forceOffReason = ref('')

// 驳回原因预设
const rejectReasons = [
  '图片不清晰',
  '信息不完整',
  '价格异常',
  '违规商品',
  '非校园商品',
  '其他'
]

// 强制下架原因预设
const forceOffReasons = [
  '违规商品',
  '虚假信息',
  '侵权内容',
  '其他'
]

// 是否有选中
const hasSelection = computed(() => selectedRows.value.length > 0)

// 加载数据
async function loadData() {
  loading.value = true
  try {
    const res = await auditApi.getPendingList(pageNum.value, pageSize.value)
    tableData.value = res.data.list
    total.value = res.data.total
  } catch (error) {
    console.error('Load pending products failed:', error)
    // Mock 数据
    tableData.value = [
      {
        productId: 1,
        productName: '大学物理教材（第四版）',
        price: 25,
        categoryName: '教材',
        publisherId: 1001,
        publisherName: '张三',
        publishTime: '2024-01-15 10:30:00',
        mainImageUrl: 'https://via.placeholder.com/100',
        productDesc: '九成新，无笔记痕迹，适合理工科学生使用'
      },
      {
        productId: 2,
        productName: '小米蓝牙耳机Air 2',
        price: 80,
        categoryName: '电子产品',
        publisherId: 1002,
        publisherName: '李四',
        publishTime: '2024-01-15 09:20:00',
        mainImageUrl: 'https://via.placeholder.com/100',
        productDesc: '使用半年，功能正常，配件齐全'
      },
      {
        productId: 3,
        productName: '台灯 护眼灯',
        price: 35,
        categoryName: '生活用品',
        publisherId: 1003,
        publisherName: '王五',
        publishTime: '2024-01-14 16:45:00',
        mainImageUrl: 'https://via.placeholder.com/100',
        productDesc: '全新未使用，买来没用上'
      }
    ]
    total.value = 3
  } finally {
    loading.value = false
  }
}

// 选择变化
function handleSelectionChange(rows: PendingProduct[]) {
  selectedRows.value = rows
}

// 分页变化
function handlePageChange(page: number) {
  pageNum.value = page
  loadData()
}

function handleSizeChange(size: number) {
  pageSize.value = size
  pageNum.value = 1
  loadData()
}

// 审核通过
async function handlePass(productId: number) {
  try {
    await ElMessageBox.confirm('确定审核通过该商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    await auditApi.pass(productId)
    ElMessage.success('审核通过')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Audit pass failed:', error)
    }
  }
}

// 打开驳回弹窗
function openRejectDialog(productId: number) {
  currentProductId.value = productId
  rejectReason.value = ''
  rejectDialogVisible.value = true
}

// 确认驳回
async function confirmReject() {
  if (!rejectReason.value) {
    ElMessage.warning('请输入驳回原因')
    return
  }
  try {
    await auditApi.reject(currentProductId.value!, rejectReason.value)
    ElMessage.success('审核驳回')
    rejectDialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('Audit reject failed:', error)
  }
}

// 批量审核通过
async function handleBatchPass() {
  if (!hasSelection.value) {
    ElMessage.warning('请选择要审核的商品')
    return
  }
  try {
    await ElMessageBox.confirm(`确定批量审核通过选中的 ${selectedRows.value.length} 个商品吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    const productIds = selectedRows.value.map((row: PendingProduct) => row.productId)
    const res = await auditApi.batchPass(productIds)
    ElMessage.success(`批量审核完成：成功 ${res.data.successCount} 个，失败 ${res.data.failCount} 个`)
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Batch audit failed:', error)
    }
  }
}

// 查看审核记录
async function viewRecords(productId: number) {
  recordsLoading.value = true
  recordsDialogVisible.value = true
  try {
    const res = await auditApi.getRecords(productId)
    auditRecords.value = res.data
  } catch (error) {
    console.error('Load audit records failed:', error)
    // Mock 数据
    auditRecords.value = [
      {
        id: 1,
        productId: productId,
        adminId: 1,
        adminName: '管理员',
        auditResult: 'REJECT',
        rejectReason: '图片不清晰',
        auditTime: '2024-01-10 14:30:00'
      }
    ]
  } finally {
    recordsLoading.value = false
  }
}

// 打开强制下架弹窗
function openForceOffDialog(productId: number) {
  currentProductId.value = productId
  forceOffReason.value = ''
  forceOffDialogVisible.value = true
}

// 确认强制下架
async function confirmForceOff() {
  if (!forceOffReason.value) {
    ElMessage.warning('请输入下架原因')
    return
  }
  try {
    await auditApi.forceOff(currentProductId.value!, forceOffReason.value)
    ElMessage.success('已强制下架')
    forceOffDialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('Force off failed:', error)
  }
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="pending-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>待审核商品列表</span>
          <el-button type="primary" :disabled="!hasSelection" @click="handleBatchPass">
            批量审核通过
          </el-button>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="tableData"
        @selection-change="handleSelectionChange"
        style="width: 100%"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column label="商品图片" width="100">
          <template #default="{ row }">
            <el-image
              :src="row.mainImageUrl"
              :preview-src-list="[row.mainImageUrl]"
              fit="cover"
              style="width: 60px; height: 60px"
            />
          </template>
        </el-table-column>
        <el-table-column prop="productName" label="商品名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column label="价格" width="100">
          <template #default="{ row }">
            ¥{{ formatMoney(row.price) }}
          </template>
        </el-table-column>
        <el-table-column prop="publisherName" label="发布者" width="100" />
        <el-table-column label="发布时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.publishTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" :icon="Check" size="small" @click="handlePass(row.productId)">
              通过
            </el-button>
            <el-button type="danger" :icon="Close" size="small" @click="openRejectDialog(row.productId)">
              驳回
            </el-button>
            <el-button :icon="View" size="small" @click="viewRecords(row.productId)">
              记录
            </el-button>
            <el-button type="warning" :icon="Delete" size="small" @click="openForceOffDialog(row.productId)">
              下架
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 驳回弹窗 -->
    <el-dialog v-model="rejectDialogVisible" title="审核驳回" width="400px">
      <el-form label-width="80px">
        <el-form-item label="驳回原因">
          <el-select v-model="rejectReason" placeholder="选择或输入驳回原因" allow-create filterable>
            <el-option v-for="reason in rejectReasons" :key="reason" :label="reason" :value="reason" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmReject">确定</el-button>
      </template>
    </el-dialog>

    <!-- 审核记录弹窗 -->
    <el-dialog v-model="recordsDialogVisible" title="审核记录" width="600px">
      <el-table v-loading="recordsLoading" :data="auditRecords" style="width: 100%">
        <el-table-column prop="adminName" label="审核人" width="100" />
        <el-table-column label="审核结果" width="100">
          <template #default="{ row }">
            <el-tag :type="row.auditResult === 'PASS' ? 'success' : 'danger'">
              {{ row.auditResult === 'PASS' ? '通过' : '驳回' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="rejectReason" label="驳回原因" show-overflow-tooltip />
        <el-table-column label="审核时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.auditTime) }}
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 强制下架弹窗 -->
    <el-dialog v-model="forceOffDialogVisible" title="强制下架" width="400px">
      <el-form label-width="80px">
        <el-form-item label="下架原因">
          <el-select v-model="forceOffReason" placeholder="选择或输入下架原因" allow-create filterable>
            <el-option v-for="reason in forceOffReasons" :key="reason" :label="reason" :value="reason" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="forceOffDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmForceOff">确定下架</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.pending-list {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
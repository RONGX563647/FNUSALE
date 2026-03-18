<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { disputeApi } from '@/api'
import { formatDateTime, disputeTypeText, disputeStatusText, processResultText } from '@/utils/format'
import { ElMessage, ElMessageBox } from 'element-plus'
import { View } from '@element-plus/icons-vue'
import type { Dispute, DisputeProcessRequest } from '@/types'

// 数据
const tableData = ref<Dispute[]>([])
const total = ref(0)
const loading = ref(false)

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  disputeStatus: ''
})

// 详情弹窗
const detailDialogVisible = ref(false)
const currentDispute = ref<Dispute | null>(null)

// 处理弹窗
const processDialogVisible = ref(false)
const processForm = reactive<DisputeProcessRequest>({
  processResult: 'BUYER_WIN',
  processRemark: '',
  buyerCreditChange: 0,
  sellerCreditChange: 0
})
const processDisputeId = ref<number | null>(null)

// 加载数据
async function loadData() {
  loading.value = true
  try {
    const res = await disputeApi.getPage(queryParams.disputeStatus, queryParams.pageNum, queryParams.pageSize)
    tableData.value = res.data.list
    total.value = res.data.total
  } catch (error) {
    console.error('Load disputes failed:', error)
    // Mock 数据
    tableData.value = [
      {
        disputeId: 1,
        orderId: 2001,
        orderNo: 'XS20240101001',
        initiatorId: 1001,
        initiatorName: '张三',
        accusedId: 1002,
        accusedName: '李四',
        disputeType: 'PRODUCT_NOT_MATCH',
        disputeStatus: 'PENDING',
        evidenceUrls: ['https://via.placeholder.com/150'],
        initiatorDescription: '收到的商品与描述不符，有明显使用痕迹',
        accusedDescription: '发货时商品完好，可能是运输造成的',
        createTime: '2024-01-15 10:00:00'
      },
      {
        disputeId: 2,
        orderId: 2002,
        orderNo: 'XS20240102001',
        initiatorId: 1003,
        initiatorName: '王五',
        accusedId: 1004,
        accusedName: '赵六',
        disputeType: 'NO_DELIVERY',
        disputeStatus: 'PROCESSING',
        evidenceUrls: [],
        initiatorDescription: '下单后卖家一直未发货',
        createTime: '2024-01-14 16:30:00'
      },
      {
        disputeId: 3,
        orderId: 2003,
        orderNo: 'XS20240103001',
        initiatorId: 1005,
        initiatorName: '孙七',
        accusedId: 1006,
        accusedName: '周八',
        disputeType: 'PRODUCT_DAMAGED',
        disputeStatus: 'RESOLVED',
        evidenceUrls: ['https://via.placeholder.com/150', 'https://via.placeholder.com/150'],
        initiatorDescription: '收到的商品已损坏',
        accusedDescription: '包装很严实，不可能是运输问题',
        createTime: '2024-01-13 09:00:00',
        processResult: 'BUYER_WIN',
        processRemark: '商品确实存在损坏，判定买家胜诉'
      }
    ]
    total.value = 3
  } finally {
    loading.value = false
  }
}

// 搜索
function handleSearch() {
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

// 查看详情
async function viewDetail(disputeId: number) {
  try {
    const res = await disputeApi.getDetail(disputeId)
    currentDispute.value = res.data
    detailDialogVisible.value = true
  } catch (error) {
    console.error('Load dispute detail failed:', error)
    currentDispute.value = tableData.value.find((d: Dispute) => d.disputeId === disputeId) || null
    detailDialogVisible.value = true
  }
}

// 打开处理弹窗
function openProcessDialog(dispute: Dispute) {
  processDisputeId.value = dispute.disputeId
  processForm.processResult = 'BUYER_WIN'
  processForm.processRemark = ''
  processForm.buyerCreditChange = 0
  processForm.sellerCreditChange = 0
  processDialogVisible.value = true
}

// 确认处理
async function confirmProcess() {
  if (!processForm.processRemark) {
    ElMessage.warning('请填写处理备注')
    return
  }
  try {
    await ElMessageBox.confirm('确定提交处理结果吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await disputeApi.process(processDisputeId.value!, processForm)
    ElMessage.success('处理成功')
    processDialogVisible.value = false
    detailDialogVisible.value = false
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Process dispute failed:', error)
    }
  }
}

// 纠纷状态标签类型
function statusType(status: string): 'primary' | 'success' | 'warning' | 'danger' | 'info' {
  const typeMap: Record<string, 'primary' | 'success' | 'warning' | 'danger' | 'info'> = {
    PENDING: 'warning',
    PROCESSING: 'info',
    RESOLVED: 'success'
  }
  return typeMap[status] || 'primary'
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="dispute-list">
    <el-card>
      <!-- 搜索栏 -->
      <template #header>
        <div class="card-header">
          <el-form :inline="true" :model="queryParams" @submit.prevent="handleSearch">
            <el-form-item label="纠纷状态">
              <el-select v-model="queryParams.disputeStatus" placeholder="全部" clearable>
                <el-option label="待处理" value="PENDING" />
                <el-option label="处理中" value="PROCESSING" />
                <el-option label="已解决" value="RESOLVED" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">搜索</el-button>
            </el-form-item>
          </el-form>
        </div>
      </template>

      <!-- 表格 -->
      <el-table v-loading="loading" :data="tableData" style="width: 100%">
        <el-table-column prop="disputeId" label="ID" width="80" />
        <el-table-column prop="orderNo" label="订单号" width="160" />
        <el-table-column label="纠纷类型" width="120">
          <template #default="{ row }">
            {{ disputeTypeText(row.disputeType) }}
          </template>
        </el-table-column>
        <el-table-column label="发起人" width="100">
          <template #default="{ row }">
            {{ row.initiatorName }}
          </template>
        </el-table-column>
        <el-table-column label="被投诉人" width="100">
          <template #default="{ row }">
            {{ row.accusedName }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.disputeStatus)" size="small">
              {{ disputeStatusText(row.disputeStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" :icon="View" size="small" @click="viewDetail(row.disputeId)">
              详情
            </el-button>
            <el-button
              v-if="row.disputeStatus !== 'RESOLVED'"
              type="success"
              size="small"
              @click="openProcessDialog(row)"
            >
              处理
            </el-button>
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

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="纠纷详情" width="600px">
      <template v-if="currentDispute">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ currentDispute.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="纠纷类型">{{ disputeTypeText(currentDispute.disputeType) }}</el-descriptions-item>
          <el-descriptions-item label="发起人">{{ currentDispute.initiatorName }}</el-descriptions-item>
          <el-descriptions-item label="被投诉人">{{ currentDispute.accusedName }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusType(currentDispute.disputeStatus)" size="small">
              {{ disputeStatusText(currentDispute.disputeStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(currentDispute.createTime) }}</el-descriptions-item>
        </el-descriptions>

        <div class="dispute-content">
          <h4>发起人描述</h4>
          <p>{{ currentDispute.initiatorDescription || '无' }}</p>

          <h4>被投诉人描述</h4>
          <p>{{ currentDispute.accusedDescription || '无' }}</p>

          <h4>举证材料</h4>
          <div v-if="currentDispute.evidenceUrls?.length" class="evidence-images">
            <el-image
              v-for="(url, index) in currentDispute.evidenceUrls"
              :key="index"
              :src="url"
              :preview-src-list="currentDispute.evidenceUrls"
              fit="cover"
              style="width: 100px; height: 100px; margin-right: 8px"
            />
          </div>
          <p v-else>无</p>

          <template v-if="currentDispute.processResult">
            <h4>处理结果</h4>
            <p>
              <el-tag type="success">{{ processResultText(currentDispute.processResult) }}</el-tag>
            </p>
            <p>处理备注：{{ currentDispute.processRemark }}</p>
          </template>
        </div>
      </template>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button
          v-if="currentDispute?.disputeStatus !== 'RESOLVED'"
          type="primary"
          @click="openProcessDialog(currentDispute!)"
        >
          处理纠纷
        </el-button>
      </template>
    </el-dialog>

    <!-- 处理弹窗 -->
    <el-dialog v-model="processDialogVisible" title="处理纠纷" width="500px">
      <el-form :model="processForm" label-width="100px">
        <el-form-item label="处理结果">
          <el-radio-group v-model="processForm.processResult">
            <el-radio value="BUYER_WIN">买家胜诉</el-radio>
            <el-radio value="SELLER_WIN">卖家胜诉</el-radio>
            <el-radio value="NEGOTIATE">协商解决</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理备注">
          <el-input
            v-model="processForm.processRemark"
            type="textarea"
            :rows="3"
            placeholder="请输入处理备注"
          />
        </el-form-item>
        <el-form-item label="买家信誉调整">
          <el-input-number v-model="processForm.buyerCreditChange" :min="-100" :max="100" />
        </el-form-item>
        <el-form-item label="卖家信誉调整">
          <el-input-number v-model="processForm.sellerCreditChange" :min="-100" :max="100" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="processDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmProcess">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.dispute-list {
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

  .dispute-content {
    margin-top: 20px;

    h4 {
      margin: 16px 0 8px;
      color: #303133;
    }

    p {
      color: #606266;
      line-height: 1.6;
    }

    .evidence-images {
      display: flex;
      flex-wrap: wrap;
    }
  }
}
</style>
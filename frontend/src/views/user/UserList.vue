<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { userApi } from '@/api'
import { formatDateTime, authStatusText, userStatusText, identityTypeText, maskPhone, maskId } from '@/utils/format'
import { ElMessage, ElMessageBox } from 'element-plus'
import { View, Lock, Unlock, Star } from '@element-plus/icons-vue'
import type { UserDetail, UserQueryParams } from '@/types'

// 数据
const tableData = ref<UserDetail[]>([])
const total = ref(0)
const loading = ref(false)

// 查询参数
const queryParams = reactive<UserQueryParams>({
  pageNum: 1,
  pageSize: 10,
  username: '',
  authStatus: undefined,
  identityType: undefined
})

// 用户详情弹窗
const detailDialogVisible = ref(false)
const currentUser = ref<UserDetail | null>(null)

// 信誉分调整弹窗
const creditDialogVisible = ref(false)
const creditScore = ref(0)
const creditReason = ref('')
const creditUserId = ref<number | null>(null)

// 加载数据
async function loadData() {
  loading.value = true
  try {
    const res = await userApi.getPage(queryParams)
    tableData.value = res.data.list
    total.value = res.data.total
  } catch (error) {
    console.error('Load users failed:', error)
    // Mock 数据
    tableData.value = [
      {
        userId: 1,
        username: '张三',
        phone: '13812345678',
        email: 'zhangsan@example.com',
        studentTeacherId: '2021001234',
        identityType: 'STUDENT',
        authStatus: 'AUTH_SUCCESS',
        creditScore: 95,
        status: 'NORMAL',
        registerTime: '2024-01-01 10:00:00',
        avatar: 'https://via.placeholder.com/40'
      },
      {
        userId: 2,
        username: '李四',
        phone: '13987654321',
        studentTeacherId: '2019805678',
        identityType: 'STUDENT',
        authStatus: 'UNDER_REVIEW',
        creditScore: 88,
        status: 'NORMAL',
        registerTime: '2024-01-02 14:30:00'
      },
      {
        userId: 3,
        username: '王五',
        phone: '13611112222',
        studentTeacherId: 'T20200123',
        identityType: 'TEACHER',
        authStatus: 'AUTH_SUCCESS',
        creditScore: 45,
        status: 'BANNED',
        registerTime: '2024-01-03 09:15:00'
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

// 重置
function handleReset() {
  queryParams.username = ''
  queryParams.authStatus = undefined
  queryParams.identityType = undefined
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
async function viewDetail(userId: number) {
  try {
    const res = await userApi.getDetail(userId)
    currentUser.value = res.data
    detailDialogVisible.value = true
  } catch (error) {
    console.error('Load user detail failed:', error)
    // Mock
    currentUser.value = tableData.value.find((u: UserDetail) => u.userId === userId) || null
    detailDialogVisible.value = true
  }
}

// 封禁用户
async function handleBan(userId: number) {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入封禁原因', '封禁用户', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /\S+/,
      inputErrorMessage: '请输入封禁原因'
    })
    await userApi.ban(userId, reason)
    ElMessage.success('封禁成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Ban user failed:', error)
    }
  }
}

// 解封用户
async function handleUnban(userId: number) {
  try {
    await ElMessageBox.confirm('确定要解封该用户吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await userApi.unban(userId)
    ElMessage.success('解封成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Unban user failed:', error)
    }
  }
}

// 打开信誉分调整弹窗
function openCreditDialog(userId: number, _currentScore: number) {
  creditUserId.value = userId
  creditScore.value = 0
  creditReason.value = ''
  creditDialogVisible.value = true
}

// 确认调整信誉分
async function confirmCredit() {
  if (!creditReason.value) {
    ElMessage.warning('请输入调整原因')
    return
  }
  try {
    await userApi.adjustCredit(creditUserId.value!, creditScore.value, creditReason.value)
    ElMessage.success('信誉分调整成功')
    creditDialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('Adjust credit failed:', error)
  }
}

// 认证状态标签类型
function authStatusType(status: string): 'primary' | 'success' | 'warning' | 'danger' | 'info' {
  const typeMap: Record<string, 'primary' | 'success' | 'warning' | 'danger' | 'info'> = {
    UNAUTH: 'info',
    UNDER_REVIEW: 'warning',
    AUTH_SUCCESS: 'success',
    AUTH_FAILED: 'danger'
  }
  return typeMap[status] || 'primary'
}

// 用户状态标签类型
function userStatusType(status: string): 'primary' | 'success' | 'warning' | 'danger' | 'info' {
  return status === 'NORMAL' ? 'success' : 'danger'
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="user-list">
    <el-card>
      <!-- 搜索栏 -->
      <template #header>
        <div class="card-header">
          <el-form :inline="true" :model="queryParams" @submit.prevent="handleSearch">
            <el-form-item label="用户名">
              <el-input v-model="queryParams.username" placeholder="请输入用户名" clearable />
            </el-form-item>
            <el-form-item label="认证状态">
              <el-select v-model="queryParams.authStatus" placeholder="全部" clearable>
                <el-option label="未认证" value="UNAUTH" />
                <el-option label="审核中" value="UNDER_REVIEW" />
                <el-option label="认证成功" value="AUTH_SUCCESS" />
                <el-option label="认证失败" value="AUTH_FAILED" />
              </el-select>
            </el-form-item>
            <el-form-item label="身份类型">
              <el-select v-model="queryParams.identityType" placeholder="全部" clearable>
                <el-option label="学生" value="STUDENT" />
                <el-option label="教职工" value="TEACHER" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">搜索</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </template>

      <!-- 表格 -->
      <el-table v-loading="loading" :data="tableData" style="width: 100%">
        <el-table-column prop="userId" label="ID" width="80" />
        <el-table-column label="用户" min-width="150">
          <template #default="{ row }">
            <div class="user-info">
              <el-avatar :size="40" :src="row.avatar" icon="User" />
              <div class="user-detail">
                <div class="username">{{ row.username }}</div>
                <div class="phone">{{ maskPhone(row.phone) }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="学号/工号" width="120">
          <template #default="{ row }">
            {{ row.studentTeacherId ? maskId(row.studentTeacherId) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="身份类型" width="100">
          <template #default="{ row }">
            {{ row.identityType ? identityTypeText(row.identityType) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="认证状态" width="100">
          <template #default="{ row }">
            <el-tag :type="authStatusType(row.authStatus)" size="small">
              {{ authStatusText(row.authStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="信誉分" width="100">
          <template #default="{ row }">
            <el-tag :type="row.creditScore >= 60 ? 'success' : 'danger'" size="small">
              {{ row.creditScore }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="userStatusType(row.status)" size="small">
              {{ userStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="注册时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.registerTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" :icon="View" size="small" @click="viewDetail(row.userId)">
              详情
            </el-button>
            <el-button
              v-if="row.status === 'NORMAL'"
              type="danger"
              :icon="Lock"
              size="small"
              @click="handleBan(row.userId)"
            >
              封禁
            </el-button>
            <el-button
              v-else
              type="success"
              :icon="Unlock"
              size="small"
              @click="handleUnban(row.userId)"
            >
              解封
            </el-button>
            <el-button :icon="Star" size="small" @click="openCreditDialog(row.userId, row.creditScore)">
              信誉
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

    <!-- 用户详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="用户详情" width="500px">
      <el-descriptions :column="2" border v-if="currentUser">
        <el-descriptions-item label="用户ID">{{ currentUser.userId }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ currentUser.username }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ maskPhone(currentUser.phone) }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ currentUser.email || '-' }}</el-descriptions-item>
        <el-descriptions-item label="学号/工号">{{ currentUser.studentTeacherId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="身份类型">{{ currentUser.identityType ? identityTypeText(currentUser.identityType) : '-' }}</el-descriptions-item>
        <el-descriptions-item label="认证状态">
          <el-tag :type="authStatusType(currentUser.authStatus)" size="small">
            {{ authStatusText(currentUser.authStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="信誉分">{{ currentUser.creditScore }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="userStatusType(currentUser.status)" size="small">
            {{ userStatusText(currentUser.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ formatDateTime(currentUser.registerTime) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 信誉分调整弹窗 -->
    <el-dialog v-model="creditDialogVisible" title="调整信誉分" width="400px">
      <el-form label-width="80px">
        <el-form-item label="调整分数">
          <el-input-number v-model="creditScore" :min="-100" :max="100" />
          <div class="credit-tip">正数加分，负数减分</div>
        </el-form-item>
        <el-form-item label="调整原因">
          <el-input v-model="creditReason" type="textarea" :rows="3" placeholder="请输入调整原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="creditDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmCredit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.user-list {
  .card-header {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
  }

  .user-info {
    display: flex;
    align-items: center;
    gap: 12px;

    .user-detail {
      .username {
        font-weight: 500;
      }
      .phone {
        font-size: 12px;
        color: #909399;
      }
    }
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }

  .credit-tip {
    margin-top: 8px;
    font-size: 12px;
    color: #909399;
  }
}
</style>
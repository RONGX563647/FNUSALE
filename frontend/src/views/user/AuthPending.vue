<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { userApi } from '@/api'
import { formatDateTime, identityTypeText } from '@/utils/format'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, Close } from '@element-plus/icons-vue'
import type { UserDetail } from '@/types'

// 数据
const tableData = ref<UserDetail[]>([])
const total = ref(0)
const loading = ref(false)

// 分页
const pageNum = ref(1)
const pageSize = ref(10)

// 加载数据
async function loadData() {
  loading.value = true
  try {
    const res = await userApi.getPendingAuthList(pageNum.value, pageSize.value)
    tableData.value = res.data.list
    total.value = res.data.total
  } catch (error) {
    console.error('Load pending auth failed:', error)
    // Mock 数据
    tableData.value = [
      {
        userId: 2,
        username: '李四',
        phone: '13987654321',
        studentTeacherId: '2021001234',
        identityType: 'STUDENT',
        authStatus: 'UNDER_REVIEW',
        creditScore: 88,
        status: 'NORMAL',
        registerTime: '2024-01-02 14:30:00',
        campusCardUrl: 'https://via.placeholder.com/300x200'
      },
      {
        userId: 4,
        username: '赵六',
        phone: '15012345678',
        studentTeacherId: 'T20210056',
        identityType: 'TEACHER',
        authStatus: 'UNDER_REVIEW',
        creditScore: 90,
        status: 'NORMAL',
        registerTime: '2024-01-10 09:00:00',
        campusCardUrl: 'https://via.placeholder.com/300x200'
      }
    ]
    total.value = 2
  } finally {
    loading.value = false
  }
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
async function handlePass(userId: number) {
  try {
    await ElMessageBox.confirm('确定审核通过该用户的认证申请吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    await userApi.authPass(userId)
    ElMessage.success('认证通过')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Auth pass failed:', error)
    }
  }
}

// 审核驳回
async function handleReject(userId: number) {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入驳回原因', '认证驳回', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /\S+/,
      inputErrorMessage: '请输入驳回原因'
    })
    await userApi.authReject(userId, reason)
    ElMessage.success('认证驳回')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Auth reject failed:', error)
    }
  }
}

// 预览校园卡图片
function previewImage(url: string) {
  ElMessageBox({
    message: `<img src="${url}" style="max-width: 100%">`,
    dangerouslyUseHTMLString: true,
    showConfirmButton: false,
    showCancelButton: true,
    cancelButtonText: '关闭'
  })
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="auth-pending">
    <el-card>
      <template #header>
        <span>待审核认证列表</span>
      </template>

      <el-table v-loading="loading" :data="tableData" style="width: 100%">
        <el-table-column prop="userId" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column label="学号/工号" width="140">
          <template #default="{ row }">
            {{ row.studentTeacherId || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="身份类型" width="100">
          <template #default="{ row }">
            <el-tag size="small">
              {{ row.identityType ? identityTypeText(row.identityType) : '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="校园卡/学生证" width="120">
          <template #default="{ row }">
            <el-button
              v-if="row.campusCardUrl"
              type="primary"
              link
              @click="previewImage(row.campusCardUrl)"
            >
              查看图片
            </el-button>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="申请时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.registerTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" :icon="Check" size="small" @click="handlePass(row.userId)">
              通过
            </el-button>
            <el-button type="danger" :icon="Close" size="small" @click="handleReject(row.userId)">
              驳回
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
  </div>
</template>

<style lang="scss" scoped>
.auth-pending {
  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
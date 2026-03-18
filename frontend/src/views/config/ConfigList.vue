<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { configApi } from '@/api'
import { formatDateTime } from '@/utils/format'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Edit } from '@element-plus/icons-vue'
import type { SystemConfig, CampusFenceConfig, SeckillConfig } from '@/types'

// 数据
const loading = ref(false)
const configList = ref<SystemConfig[]>([])

// 编辑弹窗
const editDialogVisible = ref(false)
const editingConfig = ref<SystemConfig | null>(null)
const editValue = ref('')

// 校园围栏弹窗
const fenceDialogVisible = ref(false)
const fenceConfig = ref<CampusFenceConfig>({
  fencePoints: []
})

// 秒杀配置弹窗
const seckillDialogVisible = ref(false)
const seckillConfig = ref<SeckillConfig>({
  qpsLimit: 500,
  stockPreloadMinutes: 30,
  maxBuyPerUser: 1
})

// 加载配置列表
async function loadConfigList() {
  loading.value = true
  try {
    const res = await configApi.getList()
    configList.value = res.data
  } catch (error) {
    console.error('Load config failed:', error)
    // Mock 数据
    configList.value = [
      { configKey: 'campus_fence', configValue: '配置已设置', configDesc: '校园围栏经纬度范围', updateTime: '2024-01-15 10:00:00', adminId: 1 },
      { configKey: 'seckill_qps_limit', configValue: '500', configDesc: '秒杀接口QPS阈值', updateTime: '2024-01-14 14:30:00', adminId: 1 },
      { configKey: 'seckill_stock_preload_minutes', configValue: '30', configDesc: '库存预热提前时间(分钟)', updateTime: '2024-01-14 14:30:00', adminId: 1 },
      { configKey: 'auth_expire_hours', configValue: '72', configDesc: '认证审核有效期(小时)', updateTime: '2024-01-10 09:00:00', adminId: 1 },
      { configKey: 'order_cancel_hours', configValue: '24', configDesc: '订单自动取消时间(小时)', updateTime: '2024-01-10 09:00:00', adminId: 1 },
      { configKey: 'credit_limit_score', configValue: '60', configDesc: '限制发布信誉分阈值', updateTime: '2024-01-10 09:00:00', adminId: 1 }
    ]
  } finally {
    loading.value = false
  }
}

// 打开编辑弹窗
function openEditDialog(config: SystemConfig) {
  editingConfig.value = config
  editValue.value = config.configValue
  editDialogVisible.value = true
}

// 保存编辑
async function saveEdit() {
  if (!editingConfig.value) return
  try {
    await configApi.update(editingConfig.value.configKey, editValue.value)
    ElMessage.success('保存成功')
    editDialogVisible.value = false
    loadConfigList()
  } catch (error) {
    console.error('Save config failed:', error)
  }
}

// 打开校园围栏配置
async function openFenceDialog() {
  try {
    const res = await configApi.getCampusFence()
    fenceConfig.value = res.data
  } catch (error) {
    console.error('Load fence config failed:', error)
    fenceConfig.value = {
      fencePoints: [
        { lng: 116.123, lat: 39.456 },
        { lng: 116.234, lat: 39.456 },
        { lng: 116.234, lat: 39.567 },
        { lng: 116.123, lat: 39.567 }
      ]
    }
  }
  fenceDialogVisible.value = true
}

// 保存校园围栏配置
async function saveFenceConfig() {
  try {
    await configApi.updateCampusFence(fenceConfig.value)
    ElMessage.success('保存成功')
    fenceDialogVisible.value = false
  } catch (error) {
    console.error('Save fence config failed:', error)
  }
}

// 添加围栏点
function addFencePoint() {
  fenceConfig.value.fencePoints.push({ lng: 0, lat: 0 })
}

// 删除围栏点
function removeFencePoint(index: number) {
  fenceConfig.value.fencePoints.splice(index, 1)
}

// 打开秒杀配置
async function openSeckillDialog() {
  try {
    const res = await configApi.getSeckill()
    seckillConfig.value = res.data
  } catch (error) {
    console.error('Load seckill config failed:', error)
  }
  seckillDialogVisible.value = true
}

// 保存秒杀配置
async function saveSeckillConfig() {
  try {
    await configApi.updateSeckill(seckillConfig.value)
    ElMessage.success('保存成功')
    seckillDialogVisible.value = false
  } catch (error) {
    console.error('Save seckill config failed:', error)
  }
}

// 刷新缓存
async function handleRefreshCache() {
  try {
    await ElMessageBox.confirm('确定要刷新系统配置缓存吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await configApi.refreshCache()
    ElMessage.success('缓存刷新成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Refresh cache failed:', error)
    }
  }
}

onMounted(() => {
  loadConfigList()
})
</script>

<template>
  <div class="config-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>系统配置</span>
          <div class="header-actions">
            <el-button type="primary" @click="openFenceDialog">校园围栏配置</el-button>
            <el-button type="primary" @click="openSeckillDialog">秒杀配置</el-button>
            <el-button :icon="Refresh" @click="handleRefreshCache">刷新缓存</el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="configList" style="width: 100%">
        <el-table-column prop="configKey" label="配置键" width="250" />
        <el-table-column prop="configDesc" label="配置说明" min-width="200" />
        <el-table-column prop="configValue" label="配置值" width="150">
          <template #default="{ row }">
            <span v-if="row.configKey === 'campus_fence'">已配置</span>
            <span v-else>{{ row.configValue }}</span>
          </template>
        </el-table-column>
        <el-table-column label="更新时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.updateTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button
              v-if="row.configKey !== 'campus_fence'"
              type="primary"
              :icon="Edit"
              size="small"
              @click="openEditDialog(row)"
            >
              编辑
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="editDialogVisible" title="编辑配置" width="400px">
      <el-form label-width="80px">
        <el-form-item label="配置键">
          <el-input :value="editingConfig?.configKey" disabled />
        </el-form-item>
        <el-form-item label="配置说明">
          <el-input :value="editingConfig?.configDesc" disabled />
        </el-form-item>
        <el-form-item label="配置值">
          <el-input v-model="editValue" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 校园围栏配置弹窗 -->
    <el-dialog v-model="fenceDialogVisible" title="校园围栏配置" width="600px">
      <div class="fence-config">
        <p class="tip">请配置校园围栏的经纬度坐标点，至少需要3个点形成封闭区域。</p>
        <el-table :data="fenceConfig.fencePoints" style="width: 100%">
          <el-table-column label="经度" width="200">
            <template #default="{ row }">
              <el-input-number v-model="row.lng" :precision="6" :step="0.001" controls-position="right" />
            </template>
          </el-table-column>
          <el-table-column label="纬度" width="200">
            <template #default="{ row }">
              <el-input-number v-model="row.lat" :precision="6" :step="0.001" controls-position="right" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80">
            <template #default="{ $index }">
              <el-button type="danger" size="small" @click="removeFencePoint($index)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-button type="primary" style="margin-top: 10px" @click="addFencePoint">
          添加坐标点
        </el-button>
      </div>
      <template #footer>
        <el-button @click="fenceDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveFenceConfig">保存</el-button>
      </template>
    </el-dialog>

    <!-- 秒杀配置弹窗 -->
    <el-dialog v-model="seckillDialogVisible" title="秒杀配置" width="500px">
      <el-form :model="seckillConfig" label-width="150px">
        <el-form-item label="QPS阈值">
          <el-input-number v-model="seckillConfig.qpsLimit" :min="100" :max="10000" />
        </el-form-item>
        <el-form-item label="库存预热提前时间">
          <el-input-number v-model="seckillConfig.stockPreloadMinutes" :min="5" :max="60" />
          <span style="margin-left: 8px">分钟</span>
        </el-form-item>
        <el-form-item label="每用户最大购买数">
          <el-input-number v-model="seckillConfig.maxBuyPerUser" :min="1" :max="10" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="seckillDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveSeckillConfig">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.config-list {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .header-actions {
      display: flex;
      gap: 10px;
    }
  }

  .fence-config {
    .tip {
      color: #909399;
      margin-bottom: 16px;
    }
  }
}
</style>
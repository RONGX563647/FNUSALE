import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { isAuthenticated } from '@/utils/auth'

// 路由配置
const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/LoginView.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/components/layout/AdminLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/DashboardView.vue'),
        meta: { title: '仪表盘', icon: 'Odometer' }
      },
      {
        path: 'audit',
        name: 'AuditPending',
        component: () => import('@/views/audit/PendingList.vue'),
        meta: { title: '待审核商品', icon: 'DocumentChecked' }
      },
      {
        path: 'user',
        name: 'UserList',
        component: () => import('@/views/user/UserList.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'user/auth',
        name: 'AuthPending',
        component: () => import('@/views/user/AuthPending.vue'),
        meta: { title: '认证审核', icon: 'Postcard' }
      },
      {
        path: 'dispute',
        name: 'DisputeList',
        component: () => import('@/views/dispute/DisputeList.vue'),
        meta: { title: '纠纷处理', icon: 'ChatLineSquare' }
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('@/views/statistics/StatisticsView.vue'),
        meta: { title: '数据统计', icon: 'TrendCharts' }
      },
      {
        path: 'config',
        name: 'ConfigList',
        component: () => import('@/views/config/ConfigList.vue'),
        meta: { title: '系统配置', icon: 'Setting' }
      },
      {
        path: 'log',
        name: 'LogList',
        component: () => import('@/views/log/LogList.vue'),
        meta: { title: '操作日志', icon: 'Tickets' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/NotFound.vue'),
    meta: { title: '页面不存在' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to) => {
  // 设置页面标题
  document.title = `${to.meta.title || 'FNUSALE'} - 管理后台`

  // 检查是否需要登录
  const requiresAuth = to.meta.requiresAuth !== false
  if (requiresAuth && !isAuthenticated()) {
    return { name: 'Login', query: { redirect: to.fullPath } }
  }

  // 已登录访问登录页，跳转到首页
  if (to.name === 'Login' && isAuthenticated()) {
    return { name: 'Dashboard' }
  }
})

export default router
import type {RouteRecordRaw} from 'vue-router'
import {createRouter, createWebHistory} from 'vue-router'
import {useUserStore} from '@/stores/user'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: {title: '登录', requiresAuth: false}
  },
  {
    path: '/',
    name: 'Dashboard',
    component: () => import('@/views/dashboard/index.vue'),
    meta: {title: '首页', requiresAuth: true}
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  const token = userStore.token

  // 设置页面标题
  document.title = `${to.meta.title} - Iot Universe`

  if (to.meta.requiresAuth) {
    if (!token) {
      // 未登录，跳转到登录页
      next({name: 'Login', query: {redirect: to.fullPath}})
      return
    }

    // 已登录但没有用户信息，获取用户信息
    if (!userStore.userInfo) {
      try {
        await userStore.getUserInfo()
        next()
      } catch (error) {
        // 获取用户信息失败，可能是 token 过期
        userStore.logout()
        next({name: 'Login', query: {redirect: to.fullPath}})
      }
      return
    }
  }

  // 已登录用户访问登录页，重定向到首页
  if (to.name === 'Login' && token) {
    next({name: 'Dashboard'})
    return
  }

  next()
})

export default router 
/**
 * 认证状态管理 Composable
 * 提供当前用户信息和认证状态
 */

import { ref, computed, onMounted } from 'vue'
import { authService } from '@M/auth/services/AuthService.ts'
import type { User } from '@M/auth/dtos/User.ts'

// 全局状态
const currentUser = ref<User | null>(null)
const isLoading = ref(false)
const error = ref<string | null>(null)

// 加载当前用户
async function loadCurrentUser() {
  if (!authService.isAuthenticated()) {
    currentUser.value = null
    return
  }
  
  isLoading.value = true
  error.value = null
  
  const result = await authService.getCurrentUser()
  
  if (result.isSuccess) {
    currentUser.value = result.getValue()
  } else {
    error.value = String(result.error)
    currentUser.value = null
  }
  
  isLoading.value = false
}

// 获取当前用户ID
function getCurrentUserId(): string | null {
  // 首先尝试从令牌中获取
  const tokens = authService.getTokens()
  if (tokens?.userId) {
    return tokens.userId
  }
  // 然后从用户对象中获取
  return currentUser.value?.userId || null
}

// 检查是否已认证
function isAuthenticated(): boolean {
  return authService.isAuthenticated()
}

// 注销登录
async function logout() {
  const result = await authService.logout()
  if (result.isSuccess) {
    currentUser.value = null
  }
  return result
}

// 导出 composable
export function useAuth() {
  onMounted(() => {
    // 如果还没有加载用户信息，尝试加载
    if (authService.isAuthenticated() && !currentUser.value) {
      loadCurrentUser()
    }
  })
  
  return {
    currentUser,
    isLoading,
    error,
    isAuthenticated,
    getCurrentUserId,
    loadCurrentUser,
    logout,
  }
}

// 导出单例状态供直接访问
export { currentUser, isLoading, error, getCurrentUserId, isAuthenticated, loadCurrentUser, logout }

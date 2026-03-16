<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAsyncLoader } from '@SU/async/useAsyncLoader'
import { authService } from '@M/auth/services/AuthService.ts'
import { isValidUsername, isValidPassword } from '@M/auth/utils/validation.ts'
import type { LoginRequest } from '@M/auth/dtos/User.ts'

const router = useRouter()
const { isLoading, errorMsg, executeAsync } = useAsyncLoader()

// 表单数据
const formData = ref<LoginRequest>({
  username: '',
  password: '',
})

// 表单验证错误
const formErrors = ref({
  username: '',
  password: '',
})

// 验证表单
const validateForm = (): boolean => {
  let isValid = true
  formErrors.value = { username: '', password: '' }

  // 验证用户名
  if (!formData.value.username) {
    formErrors.value.username = '请输入用户名'
    isValid = false
  } else {
    const usernameResult = isValidUsername(formData.value.username)
    if (!usernameResult.valid) {
      formErrors.value.username = usernameResult.message
      isValid = false
    }
  }

  // 验证密码
  if (!formData.value.password) {
    formErrors.value.password = '请输入密码'
    isValid = false
  } else {
    const passwordResult = isValidPassword(formData.value.password)
    if (!passwordResult.valid) {
      formErrors.value.password = passwordResult.message
      isValid = false
    }
  }

  return isValid
}

// 是否可以提交
const canSubmit = computed(() => {
  const usernameValid = formData.value.username && !formErrors.value.username
  const passwordValid = formData.value.password && !formErrors.value.password
  return usernameValid && passwordValid && !isLoading.value
})

// 提交登录
const handleSubmit = async () => {
  if (!validateForm()) return

  const result = await executeAsync(async (signal) => {
    return await authService.login(formData.value, { signal })
  }, '登录失败，请检查用户名和密码')

  if (result && result.isSuccess) {
    // 登录成功，跳转到首页
    router.push('/')
  }
}

// 跳转到注册页
const goToRegister = () => {
  router.push('/register')
}
</script>

<template>
  <div class="bf-login-container">
    <div class="bf-login-card">
      <!-- Logo/标题 -->
      <div class="bf-login-header">
        <div class="bf-logo">
          <span class="bf-logo-icon">🔥</span>
        </div>
        <h1 class="bf-login-title">欢迎回来</h1>
        <p class="bf-login-subtitle">登录您的账户继续探索</p>
      </div>

      <form @submit.prevent="handleSubmit" class="bf-login-form">
        <!-- 错误提示 -->
        <div v-if="errorMsg" class="bf-error-message">
          <svg class="bf-error-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/>
            <line x1="12" y1="8" x2="12" y2="12"/>
            <line x1="12" y1="16" x2="12.01" y2="16"/>
          </svg>
          <span>{{ errorMsg }}</span>
        </div>

        <!-- 用户名 -->
        <div class="bf-form-field">
          <label for="username" class="bf-label">用户名</label>
          <div class="bf-input-wrapper">
            <svg class="bf-input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
              <circle cx="12" cy="7" r="4"/>
            </svg>
            <input
              id="username"
              v-model="formData.username"
              type="text"
              class="bf-input"
              :class="{ 'bf-input--error': formErrors.username }"
              placeholder="请输入用户名"
              autocomplete="username"
            />
          </div>
          <span v-if="formErrors.username" class="bf-field-error">{{ formErrors.username }}</span>
        </div>

        <!-- 密码 -->
        <div class="bf-form-field">
          <label for="password" class="bf-label">密码</label>
          <div class="bf-input-wrapper">
            <svg class="bf-input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
              <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
            </svg>
            <input
              id="password"
              v-model="formData.password"
              type="password"
              class="bf-input"
              :class="{ 'bf-input--error': formErrors.password }"
              placeholder="请输入密码"
              autocomplete="current-password"
            />
          </div>
          <span v-if="formErrors.password" class="bf-field-error">{{ formErrors.password }}</span>
        </div>

        <!-- 提交按钮 -->
        <button
          type="submit"
          class="bf-btn bf-btn--primary bf-btn--full bf-btn--lg"
          :disabled="!canSubmit"
          :class="{ 'bf-btn--loading': isLoading }"
        >
          <span v-if="isLoading" class="bf-spinner"></span>
          <span v-else>登录</span>
        </button>

        <!-- 注册链接 -->
        <div class="bf-login-footer">
          <span class="bf-text--secondary">还没有账号？</span>
          <a class="bf-link" @click="goToRegister">立即注册</a>
        </div>
      </form>
    </div>
  </div>
</template>

<style scoped>
.bf-login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: var(--bf-space-lg, 24px);
}

.bf-login-card {
  width: 100%;
  max-width: 420px;
  padding: var(--bf-space-xl, 32px);
  background: var(--bf-card-bg, rgba(26, 26, 26, 0.8));
  border: 1px solid var(--bf-card-border, rgba(255, 255, 255, 0.06));
  border-radius: var(--bf-card-radius-lg, 20px);
  box-shadow: var(--bf-card-shadow, 0 4px 24px rgba(0, 0, 0, 0.4));
  backdrop-filter: blur(10px);
}

.bf-login-header {
  text-align: center;
  margin-bottom: var(--bf-space-xl, 32px);
}

.bf-logo {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  background: var(--bf-fire-gradient, linear-gradient(135deg, #FF6B35 0%, #FF9F1C 50%, #FFBE0B 100%));
  border-radius: 16px;
  margin-bottom: var(--bf-space-md, 16px);
  box-shadow: 0 4px 20px rgba(255, 107, 53, 0.3);
}

.bf-logo-icon {
  font-size: 32px;
}

.bf-login-title {
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--bf-text-primary, #FFFFFF);
  margin: 0 0 var(--bf-space-sm, 8px);
}

.bf-login-subtitle {
  font-size: 0.875rem;
  color: var(--bf-text-secondary, #B3B3B3);
  margin: 0;
}

.bf-login-form {
  display: flex;
  flex-direction: column;
  gap: var(--bf-space-md, 16px);
}

.bf-error-message {
  display: flex;
  align-items: center;
  gap: var(--bf-space-sm, 8px);
  padding: var(--bf-space-sm, 8px) var(--bf-space-md, 16px);
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.2);
  border-radius: var(--bf-input-radius, 10px);
  color: #ef4444;
  font-size: 0.875rem;
}

.bf-error-icon {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
}

.bf-form-field {
  display: flex;
  flex-direction: column;
  gap: var(--bf-space-xs, 4px);
}

.bf-label {
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--bf-text-secondary, #B3B3B3);
}

.bf-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.bf-input-icon {
  position: absolute;
  left: var(--bf-space-md, 16px);
  width: 18px;
  height: 18px;
  color: var(--bf-text-muted, #666666);
  pointer-events: none;
}

.bf-input {
  width: 100%;
  padding: var(--bf-space-sm, 8px) var(--bf-space-md, 16px);
  padding-left: 44px;
  background: var(--bf-input-bg, rgba(255, 255, 255, 0.04));
  border: 1px solid var(--bf-input-border, rgba(255, 255, 255, 0.1));
  border-radius: var(--bf-input-radius, 10px);
  color: var(--bf-text-primary, #FFFFFF);
  font-size: 0.875rem;
  transition: all var(--bf-transition-fast, 0.15s ease);
  outline: none;
}

.bf-input::placeholder {
  color: var(--bf-text-muted, #666666);
}

.bf-input:focus {
  border-color: var(--bf-primary, #FF6B35);
  box-shadow: 0 0 0 3px var(--bf-input-focus, rgba(255, 107, 53, 0.3));
}

.bf-input:hover:not(:focus) {
  border-color: rgba(255, 255, 255, 0.15);
}

.bf-input--error {
  border-color: #ef4444 !important;
}

.bf-field-error {
  font-size: 0.75rem;
  color: #ef4444;
}

.bf-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--bf-space-sm, 8px);
  padding: var(--bf-space-sm, 8px) var(--bf-space-md, 16px);
  border-radius: var(--bf-btn-radius, 12px);
  font-weight: 500;
  font-size: 0.875rem;
  transition: all var(--bf-transition-fast, 0.15s ease);
  cursor: pointer;
  border: none;
  outline: none;
}

.bf-btn--primary {
  background: var(--bf-btn-primary-bg, linear-gradient(135deg, #FF6B35 0%, #FF8C5A 100%));
  color: white;
}

.bf-btn--primary:hover:not(:disabled) {
  background: var(--bf-btn-primary-hover, linear-gradient(135deg, #FF8C5A 0%, #FFAD6B 100%));
  box-shadow: 0 4px 16px rgba(255, 107, 53, 0.3);
}

.bf-btn--full {
  width: 100%;
}

.bf-btn--lg {
  padding: var(--bf-space-md, 16px) var(--bf-space-lg, 24px);
  font-size: 1rem;
  font-weight: 600;
}

.bf-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.bf-btn--loading {
  position: relative;
}

.bf-spinner {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.bf-login-footer {
  text-align: center;
  margin-top: var(--bf-space-md, 16px);
}

.bf-text--secondary {
  color: var(--bf-text-secondary, #B3B3B3);
}

.bf-link {
  color: var(--bf-primary, #FF6B35);
  cursor: pointer;
  margin-left: var(--bf-space-xs, 4px);
  transition: color var(--bf-transition-fast, 0.15s ease);
}

.bf-link:hover {
  color: var(--bf-primary-light, #FF8C5A);
}
</style>

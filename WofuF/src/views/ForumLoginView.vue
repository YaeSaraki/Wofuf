<script setup lang="ts">
/**
 * 论坛登录页面
 * 使用 PrimeVue 组件按照标准编码规范
 */
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAsyncLoader } from '@SU/async/useAsyncLoader.ts'
import { authService } from '@M/auth/services/AuthService.ts'
import { isValidUsername, isValidPassword } from '@M/auth/utils/validation.ts'
import { translate } from '@S/services/i18n'
import InputText from 'primevue/inputtext'
import Password from 'primevue/password'
import FloatLabel from 'primevue/floatlabel'
import Button from 'primevue/button'
import Message from 'primevue/message'
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

// 当用户修改输入时清除对应错误
watch(() => formData.value.username, () => {
  if (formErrors.value.username) {
    formErrors.value.username = ''
  }
  // 清除全局错误信息
  if (errorMsg.value) {
    errorMsg.value = null
  }
})

watch(() => formData.value.password, () => {
  if (formErrors.value.password) {
    formErrors.value.password = ''
  }
  // 清除全局错误信息
  if (errorMsg.value) {
    errorMsg.value = null
  }
})

// 验证表单
const validateForm = (): boolean => {
  let isValid = true
  formErrors.value = { username: '', password: '' }

  if (!formData.value.username) {
    formErrors.value.username = translate('auth', 'errorUsernameRequired')
    isValid = false
  } else {
    const usernameResult = isValidUsername(formData.value.username)
    if (!usernameResult.valid) {
      formErrors.value.username = usernameResult.message
      isValid = false
    }
  }

  if (!formData.value.password) {
    formErrors.value.password = translate('auth', 'errorPasswordRequired')
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

const canSubmit = computed(() => {
  // 只检查是否有内容，不检查验证错误（错误会在用户输入时自动清除）
  const hasUsername = !!formData.value.username.trim()
  const hasPassword = !!formData.value.password.trim()
  return hasUsername && hasPassword && !isLoading.value
})

const handleSubmit = async () => {
  if (!validateForm()) return

  const result = await executeAsync(
    async (signal) => {
      return await authService.login(formData.value, { signal })
    },
    translate('auth', 'errorLoginFailed'),
  )

  if (result) {
    if (result.isSuccess) {
      router.push('/forum')
    } else {
      // 显示后端返回的错误信息
      errorMsg.value = String(result.error)
    }
  }
}

const goToRegister = () => router.push('/forum/register')
const goBack = () => router.push('/forum')

// 动画控制
const isLoaded = ref(false)
onMounted(() => {
  setTimeout(() => {
    isLoaded.value = true
  }, 100)
})
</script>

<template>
  <div class="login-page">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="bg-gradient"></div>
    </div>

    <!-- 返回按钮 -->
    <button class="back-btn" @click="goBack" aria-label="返回">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path stroke-linecap="round" stroke-linejoin="round" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
      </svg>
    </button>

    <!-- 登录卡片 -->
    <div class="login-card" :class="{ loaded: isLoaded }">
      <!-- 头部 -->
      <div class="card-header">
        <div class="brand-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M15.75 5.25a3 3 0 013 3m3 0a6 6 0 01-7.029 5.912c-.563-.097-1.159.026-1.563.43L10.5 17.25H8.25v2.25H6v2.25H2.25v-2.818c0-.597.237-1.17.659-1.591l6.499-6.499c.404-.404.527-1 .43-1.563A6 6 0 1121.75 8.25z" />
          </svg>
        </div>
        <h1 class="card-title">{{ translate('auth', 'welcomeBack') }}</h1>
        <p class="card-subtitle">{{ translate('auth', 'loginSubtitle') }}</p>
      </div>

      <!-- 表单 -->
      <form @submit.prevent="handleSubmit" class="login-form">
        <Message v-if="errorMsg" severity="error" :closable="false" class="error-msg">
          {{ errorMsg }}
        </Message>

        <!-- 用户名 -->
        <div class="form-group">
          <FloatLabel variant="on">
            <InputText
              id="username"
              v-model="formData.username"
              :invalid="!!formErrors.username"
              autocomplete="username"
              fluid
            />
            <label for="username">{{ translate('auth', 'username') }}</label>
          </FloatLabel>
          <small v-if="formErrors.username" class="error-text">{{ formErrors.username }}</small>
        </div>

        <!-- 密码 -->
        <div class="form-group">
          <FloatLabel variant="on">
            <Password
              id="password"
              v-model="formData.password"
              :feedback="false"
              toggleMask
              :invalid="!!formErrors.password"
              autocomplete="current-password"
              fluid
            />
            <label for="password">{{ translate('auth', 'password') }}</label>
          </FloatLabel>
          <small v-if="formErrors.password" class="error-text">{{ formErrors.password }}</small>
        </div>

        <!-- 提交按钮 -->
        <Button
          type="submit"
          :label="translate('auth', 'login')"
          :loading="isLoading"
          :disabled="!canSubmit"
          class="submit-btn"
          fluid
        />
      </form>

      <!-- 底部 -->
      <div class="card-footer">
        <span class="footer-text">{{ translate('auth', 'noAccount') }}</span>
        <a class="footer-link" @click="goToRegister">{{ translate('auth', 'registerNow') }}</a>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 页面容器 */
.login-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem 1rem;
  position: relative;
  overflow: hidden;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
}

/* 背景装饰 */
.bg-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}

.bg-gradient {
  position: absolute;
  inset: 0;
  background: #f8fafc;
}

.dark .bg-gradient {
  background: #0f172a;
}

/* 返回按钮 */
.back-btn {
  position: fixed;
  top: 1.5rem;
  left: 1.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 2.75rem;
  height: 2.75rem;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.95);
  border: none;
  color: #374151;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 100;
}

.back-btn svg {
  width: 1.25rem;
  height: 1.25rem;
}

.back-btn:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
}

.dark .back-btn {
  background: rgba(30, 30, 40, 0.95);
  color: #e5e7eb;
}

/* 登录卡片 */
.login-card {
  width: 100%;
  max-width: 420px;
  background: #ffffff;
  border-radius: 1.25rem;
  box-shadow:
    0 20px 40px rgba(0, 0, 0, 0.1),
    0 0 0 1px rgba(0, 0, 0, 0.02);
  overflow: hidden;
  opacity: 0;
  transform: translateY(20px);
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.login-card.loaded {
  opacity: 1;
  transform: translateY(0);
}

.dark .login-card {
  background: #1e1e2e;
  box-shadow:
    0 20px 40px rgba(0, 0, 0, 0.4),
    0 0 0 1px rgba(255, 255, 255, 0.05);
}

/* 头部 */
.card-header {
  padding: 2rem 2rem 1.5rem;
  text-align: center;
  border-bottom: 1px solid #f3f4f6;
}

.dark .card-header {
  border-bottom-color: rgba(255, 255, 255, 0.05);
}

.brand-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 3.5rem;
  height: 3.5rem;
  border-radius: 1rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  margin-bottom: 1rem;
}

.brand-icon svg {
  width: 1.75rem;
  height: 1.75rem;
  color: white;
}

.card-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #111827;
  margin: 0 0 0.375rem;
  letter-spacing: -0.02em;
}

.dark .card-title {
  color: #f9fafb;
}

.card-subtitle {
  font-size: 0.875rem;
  color: #6b7280;
  margin: 0;
}

.dark .card-subtitle {
  color: #9ca3af;
}

/* 表单 */
.login-form {
  padding: 1.5rem 2rem;
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.error-msg {
  border-radius: 0.75rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

/* FloatLabel 输入框样式 */
.form-group :deep(.p-floatlabel) {
  width: 100%;
}

.form-group :deep(.p-inputtext),
.form-group :deep(.p-password-input) {
  width: 100%;
  border-radius: 0.75rem;
  background: #f9fafb;
  border: 1.5px solid #e5e7eb;
  color: #111827;
  padding: 1rem 0.875rem 0.5rem;
  font-size: 0.9375rem;
  transition: all 0.2s ease;
}

.form-group :deep(.p-inputtext::placeholder),
.form-group :deep(.p-password-input::placeholder) {
  color: #9ca3af;
}

.form-group :deep(.p-inputtext:focus),
.form-group :deep(.p-password-input:focus) {
  background: #ffffff;
  border-color: #667eea;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
  outline: none;
}

.form-group :deep(.p-inputtext.p-invalid),
.form-group :deep(.p-password-input.p-invalid) {
  border-color: #ef4444;
}

.dark .form-group :deep(.p-inputtext),
.dark .form-group :deep(.p-password-input) {
  background: #2d2d3d;
  border-color: #3d3d4d;
  color: #f9fafb;
}

.dark .form-group :deep(.p-inputtext:focus),
.dark .form-group :deep(.p-password-input:focus) {
  background: #363646;
  border-color: #667eea;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.15);
}

.form-group :deep(.p-floatlabel label) {
  color: #9ca3af;
  font-size: 0.9375rem;
  top: 0.75rem;
  left: 0.875rem;
  transition: all 0.2s ease;
  background: transparent;
}

.form-group :deep(.p-floatlabel:focus-within label),
.form-group :deep(.p-floatlabel:has(.p-filled) label) {
  color: #667eea;
  font-size: 0.75rem;
  top: 0.25rem;
}

/* Password 组件样式 */
.form-group :deep(.p-password) {
  width: 100%;
}

.form-group :deep(.p-password-toggle-mask-icon) {
  right: 0.75rem;
  color: #9ca3af;
}

.form-group :deep(.p-password-toggle-mask-icon:hover) {
  color: #6b7280;
}

.dark .form-group :deep(.p-password-toggle-mask-icon) {
  color: #6b7280;
}

.error-text {
  font-size: 0.75rem;
  color: #ef4444;
}

/* 提交按钮 */
.submit-btn {
  height: 3rem;
  margin-top: 0.5rem;
  border-radius: 0.75rem;
  font-weight: 600;
  font-size: 0.9375rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  transition: all 0.2s ease;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 底部 */
.card-footer {
  padding: 1.25rem 2rem 1.75rem;
  text-align: center;
  background: #f9fafb;
  border-top: 1px solid #f3f4f6;
}

.dark .card-footer {
  background: #252535;
  border-top-color: rgba(255, 255, 255, 0.05);
}

.footer-text {
  font-size: 0.875rem;
  color: #6b7280;
}

.dark .footer-text {
  color: #9ca3af;
}

.footer-link {
  font-size: 0.875rem;
  font-weight: 600;
  color: #667eea;
  cursor: pointer;
  margin-left: 0.375rem;
  transition: color 0.2s ease;
}

.footer-link:hover {
  color: #764ba2;
}

/* 响应式 */
@media (max-width: 640px) {
  .login-page {
    padding: 1rem;
    justify-content: flex-end;
    padding-bottom: 3rem;
  }

  .login-card {
    max-width: 100%;
    border-radius: 1.25rem 1.25rem 0 0;
  }

  .card-header {
    padding: 1.5rem 1.5rem 1.25rem;
  }

  .login-form {
    padding: 1.25rem 1.5rem;
  }

  .card-footer {
    padding: 1rem 1.5rem 1.5rem;
  }

  .back-btn {
    top: 1rem;
    left: 1rem;
    width: 2.5rem;
    height: 2.5rem;
  }
}
</style>

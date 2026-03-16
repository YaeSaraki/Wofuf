<script setup lang="ts">
/**
 * 论坛登录页面
 * 使用 PrimeVue 组件按照标准编码规范
 */
import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAsyncLoader } from '@SU/async/useAsyncLoader.ts'
import { authService } from '@M/auth/services/AuthService.ts'
import { isValidUsername, isValidPassword } from '@M/auth/utils/validation.ts'
import { translate } from '@S/services/i18n'
import PageBackground from '@S/components/PageBackground.vue'
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
</script>

<template>
  <PageBackground variant="gradient">
    <div class="auth-container">
      <!-- 返回按钮 -->
      <button class="back-btn" @click="goBack" aria-label="返回">
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M10 19l-7-7m0 0l7-7m-7 7h18"
          />
        </svg>
      </button>

      <!-- 登录卡片 -->
      <div class="auth-card">
        <div class="auth-header">
          <div class="logo">
            <span class="logo-icon">🐾</span>
          </div>
          <h1 class="auth-title">{{ translate('auth', 'welcomeBack') }}</h1>
          <p class="auth-subtitle">{{ translate('auth', 'loginSubtitle') }}</p>
        </div>

        <form @submit.prevent="handleSubmit" class="auth-form">
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

          <Button
            type="submit"
            :label="translate('auth', 'login')"
            :loading="isLoading"
            :disabled="!canSubmit"
            class="submit-btn"
            fluid
          />

          <div class="auth-footer">
            <span class="footer-text">{{ translate('auth', 'noAccount') }}</span>
            <a class="footer-link" @click="goToRegister">{{ translate('auth', 'registerNow') }}</a>
          </div>
        </form>
      </div>
    </div>
  </PageBackground>
</template>

<style scoped>
.auth-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
  position: relative;
  background: var(--w-surface-alt);
}

.back-btn {
  position: absolute;
  top: 1rem;
  left: 1rem;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 2.5rem;
  height: 2.5rem;
  border-radius: var(--w-radius-full);
  background: var(--w-surface-hover);
  border: 1px solid var(--w-border);
  color: var(--w-text);
  cursor: pointer;
  transition: all var(--w-transition-fast);
}

.back-btn:hover {
  background: var(--w-border);
  transform: scale(1.05);
}

.auth-card {
  width: 100%;
  max-width: 420px;
  padding: 2rem;
  border-radius: var(--w-radius-xl);
  background: var(--w-surface-card);
  border: 1px solid var(--w-border);
  box-shadow: var(--w-shadow-xl);
}

:global(.dark) .auth-card {
  background: #1c1c1e;
  border-color: #3a3a3c;
}

.auth-header {
  text-align: center;
  margin-bottom: 2rem;
}

.logo {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 4rem;
  height: 4rem;
  border-radius: var(--w-radius-lg);
  background: var(--w-header-gradient);
  margin-bottom: 1rem;
}

.logo-icon {
  font-size: 2rem;
}

.auth-title {
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--w-text);
  margin: 0 0 0.5rem 0;
}

.auth-subtitle {
  font-size: 0.875rem;
  color: var(--w-text-muted);
  margin: 0;
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.error-msg {
  border-radius: var(--w-radius-md);
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
  border-radius: var(--w-radius-md);
  background: var(--w-surface);
  border: 1px solid var(--w-border);
  color: var(--w-text);
  padding: 1rem 0.875rem 0.5rem;
  font-size: 0.875rem;
  transition: all var(--w-transition-fast);
}

.form-group :deep(.p-inputtext::placeholder),
.form-group :deep(.p-password-input::placeholder) {
  color: var(--w-text-muted);
}

.form-group :deep(.p-inputtext:focus),
.form-group :deep(.p-password-input:focus) {
  background: var(--w-surface-card);
  border-color: var(--w-primary);
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
  outline: none;
}

.form-group :deep(.p-inputtext.p-invalid),
.form-group :deep(.p-password-input.p-invalid) {
  border-color: var(--w-error);
}

.form-group :deep(.p-floatlabel label) {
  color: var(--w-text-muted);
  font-size: 0.875rem;
  top: 0.75rem;
  left: 0.875rem;
  transition: all var(--w-transition-fast);
}

.form-group :deep(.p-floatlabel:focus-within label),
.form-group :deep(.p-floatlabel:has(.p-filled) label) {
  color: var(--w-primary);
  font-size: 0.75rem;
  top: 0.25rem;
}

/* 暗色模式 */
:global(.dark) .form-group :deep(.p-inputtext),
:global(.dark) .form-group :deep(.p-password-input) {
  background: #1c1c1e;
  border-color: #3a3a3c;
}

:global(.dark) .form-group :deep(.p-inputtext:focus),
:global(.dark) .form-group :deep(.p-password-input:focus) {
  background: #2c2c2e;
  border-color: var(--w-primary);
}

/* Password 组件样式 */
.form-group :deep(.p-password) {
  width: 100%;
}

.form-group :deep(.p-password-toggle-mask-icon) {
  right: 0.75rem;
  color: var(--w-text-muted);
}

.form-group :deep(.p-password-toggle-mask-icon:hover) {
  color: var(--w-text);
}

.error-text {
  font-size: 0.75rem;
  color: var(--w-error);
}

.submit-btn {
  height: 3rem;
  border-radius: var(--w-radius-md);
  font-weight: 600;
  margin-top: 0.5rem;
}

.auth-footer {
  text-align: center;
  padding-top: 1rem;
  border-top: 1px solid var(--w-border);
}

.footer-text {
  font-size: 0.875rem;
  color: var(--w-text-muted);
}

.footer-link {
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--w-primary);
  cursor: pointer;
  margin-left: 0.25rem;
  transition: color var(--w-transition-fast);
}

.footer-link:hover {
  color: var(--w-primary-dark);
  text-decoration: underline;
}

/* 响应式 - 移动端 */
@media (max-width: 640px) {
  .auth-container {
    padding: 0.5rem;
    align-items: flex-end;
    padding-bottom: 2rem;
  }

  .auth-card {
    max-width: 100%;
    border-radius: var(--w-radius-xl) var(--w-radius-xl) 0 0;
    padding: 1.5rem;
    margin: 0 -0.5rem;
  }

  .back-btn {
    top: 0.75rem;
    left: 0.75rem;
  }

  .auth-title {
    font-size: 1.5rem;
  }

  .logo {
    width: 3.5rem;
    height: 3.5rem;
  }

  .logo-icon {
    font-size: 1.75rem;
  }
}

@media (min-width: 641px) and (max-width: 1024px) {
  .auth-card {
    max-width: 480px;
    padding: 2.5rem;
  }
}
</style>

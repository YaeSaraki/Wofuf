<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAsyncLoader } from '@SU/async/useAsyncLoader'
import { authService } from '@M/auth/services/AuthService.ts'
import { isValidUsername, isValidPassword } from '@M/auth/utils/validation.ts'
import InputText from 'primevue/inputtext'
import Password from 'primevue/password'
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
  <div class="login-container">
    <div class="login-card">
      <h1 class="login-title">登录</h1>

      <form @submit.prevent="handleSubmit" class="login-form">
        <!-- 错误提示 -->
        <Message v-if="errorMsg" severity="error" :closable="false">
          {{ errorMsg }}
        </Message>

        <!-- 用户名 -->
        <div class="form-field">
          <label for="username">用户名</label>
          <InputText
            id="username"
            v-model="formData.username"
            placeholder="请输入用户名"
            :class="{ 'p-invalid': formErrors.username }"
            autocomplete="username"
          />
          <small v-if="formErrors.username" class="p-error">
            {{ formErrors.username }}
          </small>
        </div>

        <!-- 密码 -->
        <div class="form-field">
          <label for="password">密码</label>
          <Password
            id="password"
            v-model="formData.password"
            placeholder="请输入密码"
            :feedback="false"
            toggleMask
            :class="{ 'p-invalid': formErrors.password }"
            autocomplete="current-password"
          />
          <small v-if="formErrors.password" class="p-error">
            {{ formErrors.password }}
          </small>
        </div>

        <!-- 提交按钮 -->
        <Button
          type="submit"
          label="登录"
          :loading="isLoading"
          :disabled="!canSubmit"
          class="w-full"
        />

        <!-- 注册链接 -->
        <div class="register-link">
          <span>没有账号？</span>
          <a @click="goToRegister">立即注册</a>
        </div>
      </form>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 1rem;
}

.login-card {
  width: 100%;
  max-width: 400px;
  padding: 2rem;
  border-radius: 0.5rem;
  background: var(--p-surface-0);
  box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1);
}

@media (prefers-color-scheme: dark) {
  .login-card {
    background: var(--p-surface-900);
  }
}

.login-title {
  text-align: center;
  margin-bottom: 1.5rem;
  font-size: 1.5rem;
  font-weight: 600;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-field label {
  font-weight: 500;
}

.form-field :deep(.p-inputtext),
.form-field :deep(.p-password-input) {
  width: 100%;
}

.register-link {
  text-align: center;
  margin-top: 1rem;
}

.register-link a {
  color: var(--p-primary-color);
  cursor: pointer;
  margin-left: 0.25rem;
}

.register-link a:hover {
  text-decoration: underline;
}
</style>

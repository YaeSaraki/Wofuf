/**
 * 注册组件 - 包含用户注册和玩家绑定 - Bonfire 风格
 */

<script setup lang="ts">
import { reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAsyncLoader } from '@SU/async/useAsyncLoader'
import { authService } from '@M/auth/services/AuthService.ts'
import { memberService } from '@M/auth/services/MemberService.ts'
import { isValidUsername, isValidEmail, isValidPassword, isValidNickname, isValidPlayerId } from '@M/auth/utils/validation.ts'
import { Result } from '@S/core/Result.ts'
import type { RegisterRequest } from '@M/auth/dtos/User.ts'
import type { CreateMemberRequest } from '@M/auth/dtos/Member.ts'

const router = useRouter()
const { isLoading, errorMsg, executeAsync } = useAsyncLoader()

// 当前步骤
const currentStep = reactive({
  value: 1
})

// 用户注册数据
const userData = reactive<RegisterRequest & { confirmPassword: string }>({
  email: '',
  username: '',
  password: '',
  confirmPassword: '',
})

// 成员绑定数据
const memberData = reactive<Omit<CreateMemberRequest, 'userId'>>({
  playerId: '',
  nickName: '',
  lastPlayed: new Date().toISOString(),
  code: '',
})

// 表单验证错误
const formErrors = reactive({
  email: '',
  username: '',
  password: '',
  confirmPassword: '',
  playerId: '',
  nickName: '',
  code: '',
})

// 验证用户注册表单
const validateUserForm = (): boolean => {
  let isValid = true
  formErrors.email = ''
  formErrors.username = ''
  formErrors.password = ''
  formErrors.confirmPassword = ''

  // 验证邮箱
  if (!userData.email) {
    formErrors.email = '请输入邮箱'
    isValid = false
  } else {
    const emailResult = isValidEmail(userData.email)
    if (!emailResult.valid) {
      formErrors.email = emailResult.message
      isValid = false
    }
  }

  // 验证用户名
  if (!userData.username) {
    formErrors.username = '请输入用户名'
    isValid = false
  } else {
    const usernameResult = isValidUsername(userData.username)
    if (!usernameResult.valid) {
      formErrors.username = usernameResult.message
      isValid = false
    }
  }

  // 验证密码
  if (!userData.password) {
    formErrors.password = '请输入密码'
    isValid = false
  } else {
    const passwordResult = isValidPassword(userData.password)
    if (!passwordResult.valid) {
      formErrors.password = passwordResult.message
      isValid = false
    }
  }

  // 验证确认密码
  if (!userData.confirmPassword) {
    formErrors.confirmPassword = '请确认密码'
    isValid = false
  } else if (userData.password !== userData.confirmPassword) {
    formErrors.confirmPassword = '两次密码不一致'
    isValid = false
  }

  return isValid
}

// 验证成员绑定表单
const validateMemberForm = (): boolean => {
  let isValid = true
  formErrors.playerId = ''
  formErrors.nickName = ''
  formErrors.code = ''

  // 验证玩家ID
  if (!memberData.playerId) {
    formErrors.playerId = '请输入玩家ID'
    isValid = false
  } else if (!isValidPlayerId(memberData.playerId)) {
    formErrors.playerId = '请输入有效的玩家ID (UUID格式)'
    isValid = false
  }

  // 验证昵称
  if (!memberData.nickName) {
    formErrors.nickName = '请输入昵称'
    isValid = false
  } else {
    const nicknameResult = isValidNickname(memberData.nickName)
    if (!nicknameResult.valid) {
      formErrors.nickName = nicknameResult.message
      isValid = false
    }
  }

  // 验证验证码
  if (!memberData.code) {
    formErrors.code = '请输入验证码'
    isValid = false
  }

  return isValid
}

// 下一步
const nextStep = () => {
  if (validateUserForm()) {
    currentStep.value = 2
  }
}

// 上一步
const prevStep = () => {
  currentStep.value = 1
}

// 提交完整注册流程
const handleRegister = async () => {
  // 验证所有表单
  const isMemberValid = validateMemberForm()

  if (!isMemberValid) return

  const result = await executeAsync(async (signal) => {
    try {
      // 1. 注册用户
      const registerResult = await authService.register({
        email: userData.email,
        username: userData.username,
        password: userData.password,
      }, { signal })

      if (!registerResult.isSuccess) {
        return Result.failure(String(registerResult.error) || '用户注册失败')
      }

      const registerData = registerResult.getValue()

      // 2. 登录用户
      const loginResult = await authService.login({
        username: userData.username,
        password: userData.password,
      }, { signal })

      if (!loginResult.isSuccess) {
        // 如果登录失败，尝试删除已注册的用户（回滚）
        await authService.deleteUser({ signal }).catch(() => {})
        return Result.failure(String(loginResult.error) || '自动登录失败')
      }

      // 3. 创建成员（绑定玩家）
      const memberResult = await memberService.createMember({
        userId: registerData.userId,
        playerId: memberData.playerId,
        nickName: memberData.nickName,
        lastPlayed: memberData.lastPlayed,
        code: memberData.code,
      }, { signal })

      if (!memberResult.isSuccess) {
        // 如果绑定失败，删除用户和令牌（回滚）
        await authService.deleteUser({ signal }).catch(() => {})
        return Result.failure(String(memberResult.error) || '绑定玩家失败')
      }

      return Result.success(undefined)
    } catch (error) {
      const err = error as { message?: string }
      // 确保清理任何可能残留的数据
      try {
        await authService.deleteUser().catch(() => {})
      } catch {
        // 忽略清理错误
      }
      return Result.failure(err.message || '注册流程异常')
    }
  }, '注册失败')

  if (result && result.isSuccess) {
    router.push('/')
  }
}

// 跳转到登录页
const goToLogin = () => {
  router.push('/login')
}
</script>

<template>
  <div class="bf-register-container">
    <div class="bf-register-card">
      <!-- Logo/标题 -->
      <div class="bf-register-header">
        <div class="bf-logo">
          <span class="bf-logo-icon">🔥</span>
        </div>
        <h1 class="bf-register-title">加入我们</h1>
        <p class="bf-register-subtitle">创建账户开始你的冒险之旅</p>
      </div>

      <!-- 步骤指示器 -->
      <div class="bf-steps">
        <div class="bf-step" :class="{ 'bf-step--active': currentStep.value >= 1, 'bf-step--completed': currentStep.value > 1 }">
          <div class="bf-step-number">1</div>
          <span class="bf-step-label">账户信息</span>
        </div>
        <div class="bf-step-line" :class="{ 'bf-step-line--active': currentStep.value > 1 }"></div>
        <div class="bf-step" :class="{ 'bf-step--active': currentStep.value >= 2 }">
          <div class="bf-step-number">2</div>
          <span class="bf-step-label">绑定玩家</span>
        </div>
      </div>

      <form @submit.prevent="currentStep.value === 1 ? nextStep() : handleRegister()" class="bf-register-form">
        <!-- 错误提示 -->
        <div v-if="errorMsg" class="bf-error-message">
          <svg class="bf-error-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/>
            <line x1="12" y1="8" x2="12" y2="12"/>
            <line x1="12" y1="16" x2="12.01" y2="16"/>
          </svg>
          <span>{{ errorMsg }}</span>
        </div>

        <!-- 步骤1: 用户信息 -->
        <div v-show="currentStep.value === 1" class="bf-step-content">
          <!-- 邮箱 -->
          <div class="bf-form-field">
            <label for="email" class="bf-label">邮箱</label>
            <div class="bf-input-wrapper">
              <svg class="bf-input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
                <polyline points="22,6 12,13 2,6"/>
              </svg>
              <input
                id="email"
                v-model="userData.email"
                type="email"
                class="bf-input"
                :class="{ 'bf-input--error': formErrors.email }"
                placeholder="请输入邮箱"
                autocomplete="email"
              />
            </div>
            <span v-if="formErrors.email" class="bf-field-error">{{ formErrors.email }}</span>
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
                v-model="userData.username"
                type="text"
                class="bf-input"
                :class="{ 'bf-input--error': formErrors.username }"
                placeholder="3-50字符，仅支持字母、数字、下划线和连字符"
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
                v-model="userData.password"
                type="password"
                class="bf-input"
                :class="{ 'bf-input--error': formErrors.password }"
                placeholder="至少6个字符"
                autocomplete="new-password"
              />
            </div>
            <span v-if="formErrors.password" class="bf-field-error">{{ formErrors.password }}</span>
          </div>

          <!-- 确认密码 -->
          <div class="bf-form-field">
            <label for="confirmPassword" class="bf-label">确认密码</label>
            <div class="bf-input-wrapper">
              <svg class="bf-input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
              </svg>
              <input
                id="confirmPassword"
                v-model="userData.confirmPassword"
                type="password"
                class="bf-input"
                :class="{ 'bf-input--error': formErrors.confirmPassword }"
                placeholder="请再次输入密码"
                autocomplete="new-password"
              />
            </div>
            <span v-if="formErrors.confirmPassword" class="bf-field-error">{{ formErrors.confirmPassword }}</span>
          </div>

          <button type="submit" class="bf-btn bf-btn--primary bf-btn--full bf-btn--lg">
            下一步
          </button>
        </div>

        <!-- 步骤2: 玩家绑定 -->
        <div v-show="currentStep.value === 2" class="bf-step-content">
          <!-- 玩家ID -->
          <div class="bf-form-field">
            <label for="playerId" class="bf-label">玩家ID</label>
            <div class="bf-input-wrapper">
              <svg class="bf-input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
              </svg>
              <input
                id="playerId"
                v-model="memberData.playerId"
                type="text"
                class="bf-input"
                :class="{ 'bf-input--error': formErrors.playerId }"
                placeholder="在游戏中输入 /wofuf 获取"
              />
            </div>
            <span v-if="formErrors.playerId" class="bf-field-error">{{ formErrors.playerId }}</span>
            <span class="bf-hint">💡 在游戏中输入 /wofuf 命令获取玩家ID</span>
          </div>

          <!-- 昵称 -->
          <div class="bf-form-field">
            <label for="nickName" class="bf-label">论坛昵称</label>
            <div class="bf-input-wrapper">
              <svg class="bf-input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                <circle cx="9" cy="7" r="4"/>
                <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
                <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
              </svg>
              <input
                id="nickName"
                v-model="memberData.nickName"
                type="text"
                class="bf-input"
                :class="{ 'bf-input--error': formErrors.nickName }"
                placeholder="3-50字符，仅支持字母、数字、下划线和连字符"
              />
            </div>
            <span v-if="formErrors.nickName" class="bf-field-error">{{ formErrors.nickName }}</span>
          </div>

          <!-- 验证码 -->
          <div class="bf-form-field">
            <label for="code" class="bf-label">验证码</label>
            <div class="bf-input-wrapper">
              <svg class="bf-input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
              </svg>
              <input
                id="code"
                v-model="memberData.code"
                type="text"
                class="bf-input"
                :class="{ 'bf-input--error': formErrors.code }"
                placeholder="请输入6位验证码"
                maxlength="6"
              />
            </div>
            <span v-if="formErrors.code" class="bf-field-error">{{ formErrors.code }}</span>
            <span class="bf-hint">💡 在游戏中输入 /wofuf 命令获取验证码</span>
          </div>

          <!-- 按钮组 -->
          <div class="bf-button-group">
            <button type="button" class="bf-btn bf-btn--secondary bf-btn--lg" @click="prevStep" :disabled="isLoading">
              上一步
            </button>
            <button type="submit" class="bf-btn bf-btn--primary bf-btn--lg" :disabled="isLoading" :class="{ 'bf-btn--loading': isLoading }">
              <span v-if="isLoading" class="bf-spinner"></span>
              <span v-else>完成注册</span>
            </button>
          </div>
        </div>

        <!-- 登录链接 -->
        <div class="bf-register-footer">
          <span class="bf-text--secondary">已有账号？</span>
          <a class="bf-link" @click="goToLogin">立即登录</a>
        </div>
      </form>
    </div>
  </div>
</template>

<style scoped>
.bf-register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: var(--bf-space-lg, 24px);
}

.bf-register-card {
  width: 100%;
  max-width: 480px;
  padding: var(--bf-space-xl, 32px);
  background: var(--bf-card-bg, rgba(26, 26, 26, 0.8));
  border: 1px solid var(--bf-card-border, rgba(255, 255, 255, 0.06));
  border-radius: var(--bf-card-radius-lg, 20px);
  box-shadow: var(--bf-card-shadow, 0 4px 24px rgba(0, 0, 0, 0.4));
  backdrop-filter: blur(10px);
}

.bf-register-header {
  text-align: center;
  margin-bottom: var(--bf-space-lg, 24px);
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

.bf-register-title {
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--bf-text-primary, #FFFFFF);
  margin: 0 0 var(--bf-space-sm, 8px);
}

.bf-register-subtitle {
  font-size: 0.875rem;
  color: var(--bf-text-secondary, #B3B3B3);
  margin: 0;
}

/* 步骤指示器 */
.bf-steps {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: var(--bf-space-xl, 32px);
}

.bf-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--bf-space-xs, 4px);
}

.bf-step-number {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--bf-bg-tertiary, #242424);
  border: 2px solid var(--bf-border-default, rgba(255, 255, 255, 0.08));
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--bf-text-muted, #666666);
  transition: all var(--bf-transition-normal, 0.25s ease);
}

.bf-step--active .bf-step-number {
  background: var(--bf-fire-gradient, linear-gradient(135deg, #FF6B35 0%, #FF9F1C 50%, #FFBE0B 100%));
  border-color: transparent;
  color: white;
}

.bf-step--completed .bf-step-number {
  background: var(--bf-primary, #FF6B35);
  border-color: transparent;
  color: white;
}

.bf-step-label {
  font-size: 0.75rem;
  color: var(--bf-text-muted, #666666);
}

.bf-step--active .bf-step-label {
  color: var(--bf-text-primary, #FFFFFF);
}

.bf-step-line {
  width: 80px;
  height: 2px;
  background: var(--bf-border-default, rgba(255, 255, 255, 0.08));
  margin: 0 var(--bf-space-sm, 8px);
  margin-bottom: 20px;
  transition: background var(--bf-transition-normal, 0.25s ease);
}

.bf-step-line--active {
  background: var(--bf-primary, #FF6B35);
}

.bf-register-form {
  display: flex;
  flex-direction: column;
  gap: var(--bf-space-md, 16px);
}

.bf-step-content {
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

.bf-hint {
  font-size: 0.75rem;
  color: var(--bf-text-muted, #666666);
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

.bf-btn--secondary {
  background: var(--bf-btn-secondary-bg, rgba(255, 255, 255, 0.06));
  color: var(--bf-text-primary, #FFFFFF);
  border: 1px solid var(--bf-border-default, rgba(255, 255, 255, 0.08));
}

.bf-btn--secondary:hover:not(:disabled) {
  background: var(--bf-btn-secondary-hover, rgba(255, 255, 255, 0.1));
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

.bf-button-group {
  display: flex;
  gap: var(--bf-space-md, 16px);
}

.bf-button-group > * {
  flex: 1;
}

.bf-register-footer {
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

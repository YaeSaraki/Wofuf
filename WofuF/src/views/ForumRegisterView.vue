<script setup lang="ts">
/**
 * 论坛注册页面
 * 使用 PrimeVue 组件按照标准编码规范
 * 两步注册流程：用户信息 -> 玩家绑定
 */
import { reactive, ref, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAsyncLoader } from '@SU/async/useAsyncLoader.ts'
import { authService } from '@M/auth/services/AuthService.ts'
import { memberService } from '@M/auth/services/MemberService.ts'
import {
  isValidUsername,
  isValidEmail,
  isValidPassword,
  isValidNickname,
  isValidPlayerId,
} from '@M/auth/utils/validation.ts'
import { Result } from '@S/core/Result.ts'
import { translate } from '@S/services/i18n'
import InputText from 'primevue/inputtext'
import Password from 'primevue/password'
import Button from 'primevue/button'
import Message from 'primevue/message'
import Divider from 'primevue/divider'
import type { RegisterRequest } from '@M/auth/dtos/User.ts'
import type { CreateMemberRequest } from '@M/auth/dtos/Member.ts'

const router = useRouter()
const { isLoading, errorMsg, executeAsync } = useAsyncLoader()

const currentStep = ref(0)

const userData = reactive<RegisterRequest & { confirmPassword: string }>({
  email: '',
  username: '',
  password: '',
  confirmPassword: '',
})

const memberData = reactive<Omit<CreateMemberRequest, 'userId'>>({
  playerId: '',
  nickName: '',
  lastPlayed: new Date().toISOString(),
  code: '',
})

const formErrors = reactive({
  email: '',
  username: '',
  password: '',
  confirmPassword: '',
  playerId: '',
  nickName: '',
  code: '',
})

// 当用户修改输入时清除对应错误
watch(() => userData.email, () => { if (formErrors.email) formErrors.email = '' })
watch(() => userData.username, () => { if (formErrors.username) formErrors.username = '' })
watch(() => userData.password, () => { 
  if (formErrors.password) formErrors.password = ''
  if (formErrors.confirmPassword && userData.confirmPassword) formErrors.confirmPassword = ''
})
watch(() => userData.confirmPassword, () => { if (formErrors.confirmPassword) formErrors.confirmPassword = '' })
watch(() => memberData.playerId, () => { if (formErrors.playerId) formErrors.playerId = '' })
watch(() => memberData.nickName, () => { if (formErrors.nickName) formErrors.nickName = '' })
watch(() => memberData.code, () => { if (formErrors.code) formErrors.code = '' })

const validateUserForm = (): boolean => {
  let isValid = true
  formErrors.email = ''
  formErrors.username = ''
  formErrors.password = ''
  formErrors.confirmPassword = ''

  if (!userData.email) {
    formErrors.email = translate('auth', 'errorEmailRequired')
    isValid = false
  } else {
    const emailResult = isValidEmail(userData.email)
    if (!emailResult.valid) {
      formErrors.email = emailResult.message
      isValid = false
    }
  }

  if (!userData.username) {
    formErrors.username = translate('auth', 'errorUsernameRequired')
    isValid = false
  } else {
    const usernameResult = isValidUsername(userData.username)
    if (!usernameResult.valid) {
      formErrors.username = usernameResult.message
      isValid = false
    }
  }

  if (!userData.password) {
    formErrors.password = translate('auth', 'errorPasswordRequired')
    isValid = false
  } else {
    const passwordResult = isValidPassword(userData.password)
    if (!passwordResult.valid) {
      formErrors.password = passwordResult.message
      isValid = false
    }
  }

  if (!userData.confirmPassword) {
    formErrors.confirmPassword = translate('auth', 'errorConfirmPasswordRequired')
    isValid = false
  } else if (userData.password !== userData.confirmPassword) {
    formErrors.confirmPassword = translate('auth', 'errorPasswordMismatch')
    isValid = false
  }

  return isValid
}

const validateMemberForm = (): boolean => {
  let isValid = true
  formErrors.playerId = ''
  formErrors.nickName = ''
  formErrors.code = ''

  if (!memberData.playerId) {
    formErrors.playerId = translate('auth', 'errorPlayerIdRequired')
    isValid = false
  } else if (!isValidPlayerId(memberData.playerId)) {
    formErrors.playerId = translate('auth', 'errorPlayerIdInvalid')
    isValid = false
  }

  if (!memberData.nickName) {
    formErrors.nickName = translate('auth', 'errorNicknameRequired')
    isValid = false
  } else {
    const nicknameResult = isValidNickname(memberData.nickName)
    if (!nicknameResult.valid) {
      formErrors.nickName = nicknameResult.message
      isValid = false
    }
  }

  if (!memberData.code) {
    formErrors.code = translate('auth', 'errorCodeRequired')
    isValid = false
  }

  return isValid
}

const nextStep = () => {
  if (currentStep.value === 0 && validateUserForm()) {
    currentStep.value = 1
  }
}

const prevStep = () => {
  if (currentStep.value > 0) currentStep.value--
  else router.push('/forum/login')
}

const handleRegister = async () => {
  if (!validateMemberForm()) return

  const result = await executeAsync(
    async (signal) => {
      try {
        const registerResult = await authService.register(
          {
            email: userData.email,
            username: userData.username,
            password: userData.password,
          },
          { signal },
        )

        if (!registerResult.isSuccess) {
          return Result.failure(String(registerResult.error) || '用户注册失败')
        }

        const registerData = registerResult.getValue()

        const loginResult = await authService.login(
          {
            username: userData.username,
            password: userData.password,
          },
          { signal },
        )

        if (!loginResult.isSuccess) {
          await authService.deleteUser({ signal }).catch(() => {})
          return Result.failure(String(loginResult.error) || '自动登录失败')
        }

        const memberResult = await memberService.createMember(
          {
            userId: registerData.userId,
            playerId: memberData.playerId,
            nickName: memberData.nickName,
            lastPlayed: memberData.lastPlayed,
            code: memberData.code,
          },
          { signal },
        )

        if (!memberResult.isSuccess) {
          await authService.deleteUser({ signal }).catch(() => {})
          return Result.failure(String(memberResult.error) || '绑定玩家失败')
        }

        return Result.success(undefined)
      } catch (error) {
        const err = error as { message?: string }
        try {
          await authService.deleteUser().catch(() => {})
        } catch {}
        return Result.failure(err.message || '注册流程异常')
      }
    },
    translate('auth', 'errorRegisterFailed'),
  )

  if (result && result.isSuccess) router.push('/forum')
}

const goBack = () => router.push('/forum')
const goToLogin = () => router.push('/forum/login')

// 动画控制
const isLoaded = ref(false)
onMounted(() => {
  setTimeout(() => {
    isLoaded.value = true
  }, 100)
})
</script>

<template>
  <div class="register-page">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="bg-gradient"></div>
      <div class="bg-pattern"></div>
    </div>

    <!-- 返回按钮 -->
    <button class="back-btn" @click="goBack" aria-label="返回">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path stroke-linecap="round" stroke-linejoin="round" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
      </svg>
    </button>

    <!-- 注册卡片 -->
    <div class="register-card" :class="{ loaded: isLoaded }">
      <!-- 头部 -->
      <div class="card-header">
        <div class="brand-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M18 7.5v3m0 0v3m0-3h3m-3 0h-3m-2.25-4.125a3.375 3.375 0 11-6.75 0 3.375 3.375 0 016.75 0zM3 19.235v-.11a6.375 6.375 0 0112.75 0v.109A12.318 12.318 0 019.374 21c-2.331 0-4.512-.645-6.374-1.766z" />
          </svg>
        </div>
        <h1 class="card-title">{{ translate('auth', 'createAccount') }}</h1>
        <p class="card-subtitle">{{ translate('auth', 'registerSubtitle') }}</p>
      </div>

      <!-- 步骤指示器 -->
      <div class="steps-indicator">
        <div class="step-item" :class="{ active: currentStep >= 0, completed: currentStep > 0 }">
          <div class="step-circle">
            <span v-if="currentStep <= 0">1</span>
            <svg v-else viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd" />
            </svg>
          </div>
          <span class="step-label">{{ translate('auth', 'stepAccount') }}</span>
        </div>

        <div class="step-line" :class="{ active: currentStep > 0 }"></div>

        <div class="step-item" :class="{ active: currentStep >= 1 }">
          <div class="step-circle"><span>2</span></div>
          <span class="step-label">{{ translate('auth', 'stepBindPlayer') }}</span>
        </div>
      </div>

      <!-- 表单 -->
      <form @submit.prevent="currentStep === 0 ? nextStep() : handleRegister()" class="register-form">
        <Message v-if="errorMsg" severity="error" :closable="false" class="error-msg">
          {{ errorMsg }}
        </Message>

        <!-- 步骤1: 用户信息 -->
        <div v-if="currentStep === 0" class="form-step">
          <!-- 邮箱 -->
          <div class="form-group">
            <label class="form-label">{{ translate('auth', 'email') }}</label>
            <div class="input-wrapper">
              <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M21.75 6.75v10.5a2.25 2.25 0 01-2.25 2.25h-15a2.25 2.25 0 01-2.25-2.25V6.75m19.5 0A2.25 2.25 0 0019.5 4.5h-15a2.25 2.25 0 00-2.25 2.25m19.5 0v.243a2.25 2.25 0 01-1.07 1.916l-7.5 4.615a2.25 2.25 0 01-2.36 0L3.32 8.91a2.25 2.25 0 01-1.07-1.916V6.75" />
              </svg>
              <InputText
                v-model="userData.email"
                type="email"
                :invalid="!!formErrors.email"
                autocomplete="email"
                :placeholder="translate('auth', 'emailPlaceholder') || '请输入邮箱'"
                class="form-input"
              />
            </div>
            <small v-if="formErrors.email" class="error-text">{{ formErrors.email }}</small>
          </div>

          <!-- 用户名 -->
          <div class="form-group">
            <label class="form-label">{{ translate('auth', 'username') }}</label>
            <div class="input-wrapper">
              <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M15.75 6a3.75 3.75 0 11-7.5 0 3.75 3.75 0 017.5 0zM4.501 20.118a7.5 7.5 0 0114.998 0A17.933 17.933 0 0112 21.75c-2.676 0-5.216-.584-7.499-1.632z" />
              </svg>
              <InputText
                v-model="userData.username"
                :invalid="!!formErrors.username"
                autocomplete="username"
                :placeholder="translate('auth', 'usernamePlaceholder') || '请输入用户名'"
                class="form-input"
              />
            </div>
            <small v-if="formErrors.username" class="error-text">{{ formErrors.username }}</small>
          </div>

          <!-- 密码 -->
          <div class="form-group">
            <label class="form-label">{{ translate('auth', 'password') }}</label>
            <div class="input-wrapper">
              <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M16.5 10.5V6.75a4.5 4.5 0 10-9 0v3.75m-.75 11.25h10.5a2.25 2.25 0 002.25-2.25v-6.75a2.25 2.25 0 00-2.25-2.25H6.75a2.25 2.25 0 00-2.25 2.25v6.75a2.25 2.25 0 002.25 2.25z" />
              </svg>
              <Password
                v-model="userData.password"
                toggleMask
                :invalid="!!formErrors.password"
                autocomplete="new-password"
                :placeholder="translate('auth', 'passwordPlaceholder') || '请输入密码'"
                class="form-input"
                inputClass="password-input"
              >
                <template #footer>
                  <Divider />
                  <ul class="password-rules">
                    <li>{{ translate('auth', 'passwordRuleLength') }}</li>
                    <li>{{ translate('auth', 'passwordRuleLowercase') }}</li>
                    <li>{{ translate('auth', 'passwordRuleUppercase') }}</li>
                    <li>{{ translate('auth', 'passwordRuleNumber') }}</li>
                  </ul>
                </template>
              </Password>
            </div>
            <small v-if="formErrors.password" class="error-text">{{ formErrors.password }}</small>
          </div>

          <!-- 确认密码 -->
          <div class="form-group">
            <label class="form-label">{{ translate('auth', 'confirmPassword') }}</label>
            <div class="input-wrapper">
              <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M9 12.75L11.25 15 15 9.75m-3-7.036A11.959 11.959 0 013.598 6 11.99 11.99 0 003 9.749c0 5.592 3.824 10.29 9 11.623 5.176-1.332 9-6.03 9-11.622 0-1.31-.21-2.571-.598-3.751h-.152c-3.196 0-6.1-1.248-8.25-3.285z" />
              </svg>
              <Password
                v-model="userData.confirmPassword"
                :feedback="false"
                toggleMask
                :invalid="!!formErrors.confirmPassword"
                autocomplete="new-password"
                :placeholder="translate('auth', 'confirmPasswordPlaceholder') || '请再次输入密码'"
                class="form-input"
                inputClass="password-input"
              />
            </div>
            <small v-if="formErrors.confirmPassword" class="error-text">{{ formErrors.confirmPassword }}</small>
          </div>
        </div>

        <!-- 步骤2: 玩家绑定 -->
        <div v-else class="form-step">
          <div class="info-box">
            <svg class="info-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M11.25 11.25l.041-.02a.75.75 0 011.063.852l-.708 2.836a.75.75 0 001.063.853l.041-.021M21 12a9 9 0 11-18 0 9 9 0 0118 0zm-9-3.75h.008v.008H12V8.25z" />
            </svg>
            <span>{{ translate('auth', 'hintGameCommand') }}</span>
          </div>

          <!-- 玩家ID -->
          <div class="form-group">
            <label class="form-label">{{ translate('auth', 'playerId') }}</label>
            <div class="input-wrapper">
              <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M15.75 6a3.75 3.75 0 11-7.5 0 3.75 3.75 0 017.5 0zM4.501 20.118a7.5 7.5 0 0114.998 0A17.933 17.933 0 0112 21.75c-2.676 0-5.216-.584-7.499-1.632z" />
              </svg>
              <InputText
                v-model="memberData.playerId"
                :invalid="!!formErrors.playerId"
                :placeholder="translate('auth', 'playerIdPlaceholder') || '请输入玩家ID'"
                class="form-input"
              />
            </div>
            <small v-if="formErrors.playerId" class="error-text">{{ formErrors.playerId }}</small>
          </div>

          <!-- 昵称 -->
          <div class="form-group">
            <label class="form-label">{{ translate('auth', 'nickname') }}</label>
            <div class="input-wrapper">
              <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M15.182 15.182a4.5 4.5 0 01-6.364 0M21 12a9 9 0 11-18 0 9 9 0 0118 0zM9.75 9.75c0 .414-.168.75-.375.75S9 10.164 9 9.75 9.168 9 9.375 9s.375.336.375.75zm-.375 0h.008v.015h-.008V9.75zm5.625 0c0 .414-.168.75-.375.75s-.375-.336-.375-.75.168-.75.375-.75.375.336.375.75zm-.375 0h.008v.015h-.008V9.75z" />
              </svg>
              <InputText
                v-model="memberData.nickName"
                :invalid="!!formErrors.nickName"
                :placeholder="translate('auth', 'nicknamePlaceholder') || '请输入昵称'"
                class="form-input"
              />
            </div>
            <small v-if="formErrors.nickName" class="error-text">{{ formErrors.nickName }}</small>
          </div>

          <!-- 验证码 -->
          <div class="form-group">
            <label class="form-label">{{ translate('auth', 'code') }}</label>
            <div class="input-wrapper">
              <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M9 12.75L11.25 15 15 9.75m-3-7.036A11.959 11.959 0 013.598 6 11.99 11.99 0 003 9.749c0 5.592 3.824 10.29 9 11.623 5.176-1.332 9-6.03 9-11.622 0-1.31-.21-2.571-.598-3.751h-.152c-3.196 0-6.1-1.248-8.25-3.285z" />
              </svg>
              <InputText
                v-model="memberData.code"
                maxlength="6"
                :invalid="!!formErrors.code"
                :placeholder="translate('auth', 'codePlaceholder') || '请输入验证码'"
                class="form-input"
              />
            </div>
            <small v-if="formErrors.code" class="error-text">{{ formErrors.code }}</small>
          </div>
        </div>

        <!-- 按钮组 -->
        <div class="button-group">
          <Button
            type="button"
            :label="currentStep === 0 ? translate('auth', 'back') : translate('auth', 'prevStep')"
            severity="secondary"
            outlined
            @click="prevStep"
            :disabled="isLoading"
            class="back-button"
          />
          <Button
            type="submit"
            :label="currentStep === 0 ? translate('auth', 'nextStep') : translate('auth', 'register')"
            :loading="isLoading"
            class="submit-btn"
          />
        </div>
      </form>

      <!-- 底部 -->
      <div v-if="currentStep === 0" class="card-footer">
        <span class="footer-text">{{ translate('auth', 'hasAccount') }}</span>
        <a class="footer-link" @click="goToLogin">{{ translate('auth', 'loginNow') }}</a>
      </div>
    </div>

    <!-- 品牌标识 -->
    <div class="brand-footer">
      <span class="brand-name">WofuF</span>
      <span class="brand-divider">·</span>
      <span class="brand-year">2026</span>
    </div>
  </div>
</template>

<style scoped>
/* 页面容器 */
.register-page {
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

.bg-pattern {
  display: none;
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

/* 注册卡片 */
.register-card {
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

.register-card.loaded {
  opacity: 1;
  transform: translateY(0);
}

.dark .register-card {
  background: #1e1e2e;
  box-shadow: 
    0 20px 40px rgba(0, 0, 0, 0.4),
    0 0 0 1px rgba(255, 255, 255, 0.05);
}

/* 头部 */
.card-header {
  padding: 1.75rem 2rem 1.25rem;
  text-align: center;
}

.brand-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 3.5rem;
  height: 3.5rem;
  border-radius: 1rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  margin-bottom: 0.875rem;
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

/* 步骤指示器 */
.steps-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  padding: 0 2rem;
  margin-bottom: 1rem;
}

.step-line {
  position: absolute;
  top: 1rem;
  left: calc(50% - 1.5rem);
  right: calc(50% - 1.5rem);
  height: 2px;
  background: #e5e7eb;
  z-index: 0;
  transition: background 0.3s ease;
}

.dark .step-line {
  background: #374151;
}

.step-line.active {
  background: linear-gradient(90deg, #667eea, #764ba2);
}

.step-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  z-index: 1;
  padding: 0 0.75rem;
}

.step-circle {
  width: 2rem;
  height: 2rem;
  border-radius: 50%;
  background: #f3f4f6;
  border: 2px solid #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
  font-weight: 600;
  color: #6b7280;
  transition: all 0.3s ease;
}

.dark .step-circle {
  background: #374151;
  border-color: #4b5563;
  color: #9ca3af;
}

.step-circle svg {
  width: 0.875rem;
  height: 0.875rem;
}

.step-item.active .step-circle,
.step-item.completed .step-circle {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: transparent;
  color: white;
}

.step-label {
  font-size: 0.6875rem;
  color: #6b7280;
  font-weight: 500;
}

.dark .step-label {
  color: #9ca3af;
}

.step-item.active .step-label {
  color: #667eea;
}

/* 表单 */
.register-form {
  padding: 0 2rem 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.error-msg {
  border-radius: 0.75rem;
}

.form-step {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-label {
  font-size: 0.8125rem;
  font-weight: 600;
  color: #374151;
  letter-spacing: 0.01em;
}

.dark .form-label {
  color: #d1d5db;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 0.875rem;
  width: 1.125rem;
  height: 1.125rem;
  color: #9ca3af;
  pointer-events: none;
  z-index: 1;
}

.dark .input-icon {
  color: #6b7280;
}

.form-input {
  width: 100%;
  padding: 0.875rem 0.875rem 0.875rem 2.75rem;
  border-radius: 0.75rem;
  border: 1.5px solid #e5e7eb;
  background: #f9fafb;
  color: #111827;
  font-size: 0.9375rem;
  transition: all 0.2s ease;
}

.form-input::placeholder {
  color: #9ca3af;
}

.form-input:focus {
  outline: none;
  border-color: #667eea;
  background: #ffffff;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
}

.form-input.p-invalid {
  border-color: #ef4444;
}

.dark .form-input {
  background: #2d2d3d;
  border-color: #3d3d4d;
  color: #f9fafb;
}

.dark .form-input::placeholder {
  color: #6b7280;
}

.dark .form-input:focus {
  background: #363646;
  border-color: #667eea;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.15);
}

/* Password组件样式 */
.input-wrapper:deep(.p-password) {
  width: 100%;
  position: relative;
}

.input-wrapper:deep(.p-password-input) {
  width: 100%;
  padding: 0.875rem 2.5rem 0.875rem 2.75rem;
  border-radius: 0.75rem;
  border: 1.5px solid #e5e7eb;
  background: #f9fafb;
  color: #111827;
  font-size: 0.9375rem;
  transition: all 0.2s ease;
}

.input-wrapper:deep(.p-password-input:focus) {
  outline: none;
  border-color: #667eea;
  background: #ffffff;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
}

.dark .input-wrapper:deep(.p-password-input) {
  background: #2d2d3d;
  border-color: #3d3d4d;
  color: #f9fafb;
}

.dark .input-wrapper:deep(.p-password-input:focus) {
  background: #363646;
  border-color: #667eea;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.15);
}

.input-wrapper:deep(.p-password-toggle-mask-icon) {
  right: 0.75rem;
  color: #9ca3af;
}

.input-wrapper:deep(.p-password-panel) {
  border-radius: 0.75rem;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
}

.dark .input-wrapper:deep(.p-password-panel) {
  background: #2d2d3d;
  border-color: #3d3d4d;
}

.password-rules {
  padding-left: 1.25rem;
  margin: 0.5rem 0;
  font-size: 0.75rem;
  color: #6b7280;
  line-height: 1.6;
}

.dark .password-rules {
  color: #9ca3af;
}

.password-rules li {
  margin-bottom: 0.25rem;
}

.error-text {
  font-size: 0.75rem;
  color: #ef4444;
  margin-top: 0.125rem;
}

/* 信息提示框 */
.info-box {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  padding: 0.875rem;
  background: rgba(102, 126, 234, 0.1);
  border-radius: 0.75rem;
  font-size: 0.8125rem;
  color: #667eea;
  line-height: 1.5;
}

.dark .info-box {
  background: rgba(102, 126, 234, 0.15);
}

.info-icon {
  width: 1.25rem;
  height: 1.25rem;
  flex-shrink: 0;
  margin-top: 0.0625rem;
}

/* 按钮组 */
.button-group {
  display: flex;
  gap: 0.75rem;
  margin-top: 0.5rem;
}

.back-button {
  flex: 0 0 auto;
  min-width: 100px;
  height: 2.75rem;
  border-radius: 0.75rem;
  font-weight: 600;
}

.submit-btn {
  flex: 1;
  height: 2.75rem;
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

/* 底部 */
.card-footer {
  padding: 1rem 2rem 1.5rem;
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

/* 品牌标识 */
.brand-footer {
  position: absolute;
  bottom: 1.5rem;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: rgba(0, 0, 0, 0.3);
  font-size: 0.75rem;
  letter-spacing: 0.05em;
}

.dark .brand-footer {
  color: rgba(255, 255, 255, 0.4);
}

.brand-name {
  font-weight: 600;
}

.brand-divider {
  opacity: 0.5;
}

/* 响应式 */
@media (max-width: 640px) {
  .register-page {
    padding: 1rem;
    justify-content: flex-end;
    padding-bottom: 3rem;
  }

  .register-card {
    max-width: 100%;
    border-radius: 1.25rem 1.25rem 0 0;
  }

  .card-header {
    padding: 1.25rem 1.5rem 1rem;
  }

  .register-form {
    padding: 0 1.5rem 1.25rem;
  }

  .card-footer {
    padding: 0.875rem 1.5rem 1.25rem;
  }

  .back-btn {
    top: 1rem;
    left: 1rem;
    width: 2.5rem;
    height: 2.5rem;
  }

  .brand-footer {
    display: none;
  }
}
</style>

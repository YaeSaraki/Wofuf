<script setup lang="ts">
/**
 * 论坛注册页面
 * 使用 PrimeVue 组件按照标准编码规范
 * 两步注册流程：用户信息 -> 玩家绑定
 */
import { reactive, ref, watch } from 'vue'
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
import PageBackground from '@S/components/PageBackground.vue'
import InputText from 'primevue/inputtext'
import Password from 'primevue/password'
import FloatLabel from 'primevue/floatlabel'
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

      <!-- 注册卡片 -->
      <div class="auth-card">
        <div class="auth-header">
          <div class="logo">
            <span class="logo-icon">🐾</span>
          </div>
          <h1 class="auth-title">{{ translate('auth', 'createAccount') }}</h1>
          <p class="auth-subtitle">{{ translate('auth', 'registerSubtitle') }}</p>
        </div>

        <!-- 步骤指示器 -->
        <div class="steps-indicator">
          <div class="step-item" :class="{ active: currentStep >= 0, completed: currentStep > 0 }">
            <div class="step-circle">
              <span v-if="currentStep <= 0">1</span>
              <svg v-else class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                <path
                  fill-rule="evenodd"
                  d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
                  clip-rule="evenodd"
                />
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

        <form @submit.prevent="currentStep === 0 ? nextStep() : handleRegister()" class="auth-form">
          <Message v-if="errorMsg" severity="error" :closable="false" class="error-msg">
            {{ errorMsg }}
          </Message>

          <!-- 步骤1: 用户信息 -->
          <div v-if="currentStep === 0" class="form-step">
            <!-- 邮箱 -->
            <div class="form-group">
              <FloatLabel variant="on">
                <InputText
                  id="email"
                  v-model="userData.email"
                  type="email"
                  :invalid="!!formErrors.email"
                  autocomplete="email"
                  fluid
                />
                <label for="email">{{ translate('auth', 'email') }}</label>
              </FloatLabel>
              <small v-if="formErrors.email" class="error-text">{{ formErrors.email }}</small>
            </div>

            <!-- 用户名 -->
            <div class="form-group">
              <FloatLabel variant="on">
                <InputText
                  id="username"
                  v-model="userData.username"
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
                  v-model="userData.password"
                  toggleMask
                  :invalid="!!formErrors.password"
                  autocomplete="new-password"
                  fluid
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
                <label for="password">{{ translate('auth', 'password') }}</label>
              </FloatLabel>
              <small v-if="formErrors.password" class="error-text">{{ formErrors.password }}</small>
            </div>

            <!-- 确认密码 -->
            <div class="form-group">
              <FloatLabel variant="on">
                <Password
                  id="confirmPassword"
                  v-model="userData.confirmPassword"
                  :feedback="false"
                  toggleMask
                  :invalid="!!formErrors.confirmPassword"
                  autocomplete="new-password"
                  fluid
                />
                <label for="confirmPassword">{{ translate('auth', 'confirmPassword') }}</label>
              </FloatLabel>
              <small v-if="formErrors.confirmPassword" class="error-text">
                {{ formErrors.confirmPassword }}
              </small>
            </div>
          </div>

          <!-- 步骤2: 玩家绑定 -->
          <div v-else class="form-step">
            <div class="info-box">
              <svg class="info-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                />
              </svg>
              <span>{{ translate('auth', 'hintGameCommand') }}</span>
            </div>

            <!-- 玩家ID -->
            <div class="form-group">
              <FloatLabel variant="on">
                <InputText
                  id="playerId"
                  v-model="memberData.playerId"
                  :invalid="!!formErrors.playerId"
                  fluid
                />
                <label for="playerId">{{ translate('auth', 'playerId') }}</label>
              </FloatLabel>
              <small v-if="formErrors.playerId" class="error-text">{{ formErrors.playerId }}</small>
            </div>

            <!-- 昵称 -->
            <div class="form-group">
              <FloatLabel variant="on">
                <InputText
                  id="nickName"
                  v-model="memberData.nickName"
                  :invalid="!!formErrors.nickName"
                  fluid
                />
                <label for="nickName">{{ translate('auth', 'nickname') }}</label>
              </FloatLabel>
              <small v-if="formErrors.nickName" class="error-text">{{ formErrors.nickName }}</small>
            </div>

            <!-- 验证码 -->
            <div class="form-group">
              <FloatLabel variant="on">
                <InputText
                  id="code"
                  v-model="memberData.code"
                  maxlength="6"
                  :invalid="!!formErrors.code"
                  fluid
                />
                <label for="code">{{ translate('auth', 'code') }}</label>
              </FloatLabel>
              <small v-if="formErrors.code" class="error-text">{{ formErrors.code }}</small>
            </div>
          </div>

          <div class="button-group">
            <Button
              type="button"
              :label="currentStep === 0 ? translate('auth', 'login') : translate('auth', 'prevStep')"
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
              fluid
            />
          </div>

          <div v-if="currentStep === 0" class="auth-footer">
            <span class="footer-text">{{ translate('auth', 'hasAccount') }}</span>
            <a class="footer-link" @click="goToLogin">{{ translate('auth', 'loginNow') }}</a>
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
  max-width: 440px;
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
  margin-bottom: 1.5rem;
}

.logo {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 3.5rem;
  height: 3.5rem;
  border-radius: var(--w-radius-lg);
  background: var(--w-header-gradient);
  margin-bottom: 0.75rem;
}

.logo-icon {
  font-size: 1.75rem;
}

.auth-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--w-text);
  margin: 0 0 0.25rem 0;
}

.auth-subtitle {
  font-size: 0.875rem;
  color: var(--w-text-muted);
  margin: 0;
}

/* 步骤指示器 */
.steps-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  margin-bottom: 1.5rem;
}

.step-line {
  position: absolute;
  top: 50%;
  left: 25%;
  right: 25%;
  height: 2px;
  background: var(--w-border);
  transform: translateY(-50%);
  z-index: 0;
  transition: background var(--w-transition-normal);
}

.step-line.active {
  background: var(--w-primary);
}

.step-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  z-index: 1;
  padding: 0 1.5rem;
}

.step-circle {
  width: 2rem;
  height: 2rem;
  border-radius: var(--w-radius-full);
  background: var(--w-surface-hover);
  border: 2px solid var(--w-border);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--w-text-muted);
  transition: all var(--w-transition-normal);
}

.step-item.active .step-circle,
.step-item.completed .step-circle {
  background: var(--w-primary);
  border-color: var(--w-primary);
  color: white;
}

.step-label {
  font-size: 0.75rem;
  color: var(--w-text-muted);
  font-weight: 500;
}

.step-item.active .step-label {
  color: var(--w-primary);
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.form-step {
  display: flex;
  flex-direction: column;
  gap: 1rem;
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

.form-group :deep(.p-password-panel) {
  border-radius: var(--w-radius-md);
  background: var(--w-surface-card);
  border: 1px solid var(--w-border);
  box-shadow: var(--w-shadow-lg);
}

.password-rules {
  padding-left: 1.25rem;
  margin: 0.5rem 0;
  font-size: 0.75rem;
  color: var(--w-text-muted);
  line-height: 1.6;
}

.password-rules li {
  margin-bottom: 0.25rem;
}

.error-text {
  font-size: 0.75rem;
  color: var(--w-error);
}

.info-box {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  padding: 0.875rem;
  background: rgba(59, 130, 246, 0.1);
  border-radius: var(--w-radius-md);
  font-size: 0.8125rem;
  color: var(--w-primary);
}

:global(.dark) .info-box {
  background: rgba(59, 130, 246, 0.15);
}

.info-icon {
  width: 1.25rem;
  height: 1.25rem;
  flex-shrink: 0;
}

.button-group {
  display: flex;
  gap: 0.75rem;
  margin-top: 0.5rem;
}

.back-button {
  flex: 0 0 auto;
  min-width: 100px;
}

.submit-btn {
  flex: 1;
  height: 2.75rem;
  border-radius: var(--w-radius-md);
  font-weight: 600;
}

.auth-footer {
  text-align: center;
  padding-top: 0.75rem;
  border-top: 1px solid var(--w-border);
  margin-top: 0.5rem;
}

.footer-text {
  font-size: 0.8125rem;
  color: var(--w-text-muted);
}

.footer-link {
  font-size: 0.8125rem;
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

/* 响应式 */
@media (max-width: 640px) {
  .auth-container {
    padding: 0.5rem;
    align-items: flex-end;
    padding-bottom: 1.5rem;
  }

  .auth-card {
    max-width: 100%;
    border-radius: var(--w-radius-xl) var(--w-radius-xl) 0 0;
    padding: 1.25rem;
    margin: 0 -0.5rem;
  }

  .back-btn {
    top: 0.75rem;
    left: 0.75rem;
  }

  .logo {
    width: 3rem;
    height: 3rem;
  }

  .logo-icon {
    font-size: 1.5rem;
  }

  .auth-title {
    font-size: 1.25rem;
  }

  .auth-subtitle {
    font-size: 0.8125rem;
  }

  .step-item {
    padding: 0 1rem;
  }

  .step-label {
    font-size: 0.6875rem;
  }

  .info-box {
    font-size: 0.75rem;
    padding: 0.75rem;
  }
}

@media (min-width: 641px) and (max-width: 1024px) {
  .auth-card {
    max-width: 480px;
    padding: 2rem;
  }
}
</style>

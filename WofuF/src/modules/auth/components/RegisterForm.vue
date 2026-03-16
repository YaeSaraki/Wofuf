/**
 * 注册组件 - 包含用户注册和玩家绑定
 */

<script setup lang="ts">
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAsyncLoader } from '@SU/async/useAsyncLoader'
import { authService } from '@M/auth/services/AuthService.ts'
import { memberService } from '@M/auth/services/MemberService.ts'
import { isValidUsername, isValidEmail, isValidPassword, isValidNickname, isValidPlayerId } from '@M/auth/utils/validation.ts'
import { Result } from '@S/core/Result.ts'
import InputText from 'primevue/inputtext'
import Password from 'primevue/password'
import Button from 'primevue/button'
import Message from 'primevue/message'
import type { RegisterRequest } from '@M/auth/dtos/User.ts'
import type { CreateMemberRequest } from '@M/auth/dtos/Member.ts'

const router = useRouter()
const { isLoading, errorMsg, executeAsync } = useAsyncLoader()

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

// 提交完整注册流程
const handleRegister = async () => {
  // 验证所有表单
  const isUserValid = validateUserForm()
  const isMemberValid = validateMemberForm()

  if (!isUserValid || !isMemberValid) return

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
  <div class="register-container">
    <div class="register-card">
      <h1 class="register-title">注册</h1>

      <form @submit.prevent="handleRegister" class="register-form">
        <!-- 错误提示 -->
        <Message v-if="errorMsg" severity="error" :closable="false">
          {{ errorMsg }}
        </Message>

        <!-- 邮箱 -->
        <div class="form-field">
          <label for="email">邮箱</label>
          <InputText
            id="email"
            v-model="userData.email"
            type="email"
            placeholder="请输入邮箱"
            :class="{ 'p-invalid': formErrors.email }"
            autocomplete="email"
          />
          <small v-if="formErrors.email" class="p-error">{{ formErrors.email }}</small>
        </div>

        <!-- 用户名 -->
        <div class="form-field">
          <label for="username">用户名</label>
          <InputText
            id="username"
            v-model="userData.username"
            placeholder="3-50字符，仅支持字母、数字、下划线和连字符"
            :class="{ 'p-invalid': formErrors.username }"
            autocomplete="username"
          />
          <small v-if="formErrors.username" class="p-error">{{ formErrors.username }}</small>
        </div>

        <!-- 密码 -->
        <div class="form-field">
          <label for="password">密码</label>
          <Password
            id="password"
            v-model="userData.password"
            placeholder="至少6个字符"
            toggleMask
            :class="{ 'p-invalid': formErrors.password }"
            autocomplete="new-password"
          />
          <small v-if="formErrors.password" class="p-error">{{ formErrors.password }}</small>
        </div>

        <!-- 确认密码 -->
        <div class="form-field">
          <label for="confirmPassword">确认密码</label>
          <Password
            id="confirmPassword"
            v-model="userData.confirmPassword"
            placeholder="请再次输入密码"
            :feedback="false"
            toggleMask
            :class="{ 'p-invalid': formErrors.confirmPassword }"
            autocomplete="new-password"
          />
          <small v-if="formErrors.confirmPassword" class="p-error">{{ formErrors.confirmPassword }}</small>
        </div>

        <!-- 玩家ID -->
        <div class="form-field">
          <label for="playerId">玩家ID</label>
          <InputText
            id="playerId"
            v-model="memberData.playerId"
            placeholder="在游戏中输入 /wofuf 获取"
            :class="{ 'p-invalid': formErrors.playerId }"
          />
          <small v-if="formErrors.playerId" class="p-error">{{ formErrors.playerId }}</small>
          <small class="hint">在游戏中输入 /wofuf 命令获取玩家ID</small>
        </div>

        <!-- 昵称 -->
        <div class="form-field">
          <label for="nickName">论坛昵称</label>
          <InputText
            id="nickName"
            v-model="memberData.nickName"
            placeholder="3-50字符，仅支持字母、数字、下划线和连字符"
            :class="{ 'p-invalid': formErrors.nickName }"
          />
          <small v-if="formErrors.nickName" class="p-error">{{ formErrors.nickName }}</small>
        </div>

        <!-- 验证码 -->
        <div class="form-field">
          <label for="code">验证码</label>
          <InputText
            id="code"
            v-model="memberData.code"
            placeholder="请输入6位验证码"
            maxlength="6"
            :class="{ 'p-invalid': formErrors.code }"
          />
          <small v-if="formErrors.code" class="p-error">{{ formErrors.code }}</small>
          <small class="hint">在游戏中输入 /wofuf 命令获取验证码</small>
        </div>

        <!-- 按钮组 -->
        <div class="button-group">
          <Button
            type="button"
            label="返回登录"
            severity="secondary"
            outlined
            @click="goToLogin"
            :disabled="isLoading"
          />
          <Button
            type="submit"
            label="注册"
            :loading="isLoading"
          />
        </div>
      </form>
    </div>
  </div>
</template>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 1rem;
}

.register-card {
  width: 100%;
  max-width: 450px;
  padding: 2rem;
  border-radius: 0.5rem;
  box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1);
}


.register-title {
  text-align: center;
  margin-bottom: 1.5rem;
  font-size: 1.5rem;
  font-weight: 600;
}

.register-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.form-field label {
  font-weight: 500;
}

.form-field :deep(.p-inputtext),
.form-field :deep(.p-password-input) {
  width: 100%;
}

.hint {
  font-size: 0.75rem;
}

.button-group {
  display: flex;
  gap: 1rem;
  margin-top: 0.5rem;
}

.button-group > * {
  flex: 1;
}
</style>

/**
 * 翻译配置 - 认证模块
 */

import { registerTranslations } from '@S/services/i18n'

const authTranslations = {
  // 表单标签
  username: {
    zh: '用户名',
    en: 'Username',
  },
  email: {
    zh: '邮箱',
    en: 'Email',
  },
  password: {
    zh: '密码',
    en: 'Password',
  },
  confirmPassword: {
    zh: '确认密码',
    en: 'Confirm Password',
  },
  nickname: {
    zh: '昵称',
    en: 'Nickname',
  },
  playerId: {
    zh: '玩家ID',
    en: 'Player ID',
  },
  code: {
    zh: '验证码',
    en: 'Verification Code',
  },

  // 按钮
  login: {
    zh: '登录',
    en: 'Login',
  },
  register: {
    zh: '注册',
    en: 'Register',
  },
  logout: {
    zh: '登出',
    en: 'Logout',
  },
  nextStep: {
    zh: '下一步',
    en: 'Next',
  },
  prevStep: {
    zh: '上一步',
    en: 'Previous',
  },
  submit: {
    zh: '提交',
    en: 'Submit',
  },

  // 页面标题
  welcomeBack: {
    zh: '欢迎回来',
    en: 'Welcome Back',
  },
  createAccount: {
    zh: '创建账号',
    en: 'Create Account',
  },
  loginSubtitle: {
    zh: '登录您的账号，参与社区讨论',
    en: 'Sign in to join the community',
  },
  registerSubtitle: {
    zh: '加入社区，开始你的冒险',
    en: 'Join the community, start your adventure',
  },

  // 步骤
  stepAccount: {
    zh: '账号信息',
    en: 'Account',
  },
  stepBindPlayer: {
    zh: '绑定玩家',
    en: 'Bind Player',
  },

  // 提示
  hintUsername: {
    zh: '字母、数字、下划线、连字符',
    en: 'Letters, numbers, underscores, hyphens',
  },
  hintPassword: {
    zh: '至少6个字符',
    en: 'At least 6 characters',
  },
  hintPlayerId: {
    zh: 'UUID 格式',
    en: 'UUID format',
  },
  hintCode: {
    zh: '6位验证码',
    en: '6-digit code',
  },
  hintGameCommand: {
    zh: '请在游戏中输入 /wofuf 命令获取玩家ID和验证码',
    en: 'Type /wofuf in game to get your Player ID and verification code',
  },

  // 密码规则
  passwordRuleLength: {
    zh: '至少6个字符',
    en: 'At least 6 characters',
  },
  passwordRuleLowercase: {
    zh: '包含小写字母',
    en: 'At least one lowercase',
  },
  passwordRuleUppercase: {
    zh: '包含大写字母',
    en: 'At least one uppercase',
  },
  passwordRuleNumber: {
    zh: '包含数字',
    en: 'At least one number',
  },

  // 链接文本
  noAccount: {
    zh: '还没有账号？',
    en: "Don't have an account?",
  },
  hasAccount: {
    zh: '已有账号？',
    en: 'Already have an account?',
  },
  registerNow: {
    zh: '立即注册',
    en: 'Register now',
  },
  loginNow: {
    zh: '立即登录',
    en: 'Login now',
  },

  // 错误信息
  errorUsernameRequired: {
    zh: '请输入用户名',
    en: 'Username is required',
  },
  errorEmailRequired: {
    zh: '请输入邮箱',
    en: 'Email is required',
  },
  errorPasswordRequired: {
    zh: '请输入密码',
    en: 'Password is required',
  },
  errorConfirmPasswordRequired: {
    zh: '请确认密码',
    en: 'Please confirm your password',
  },
  errorPasswordMismatch: {
    zh: '两次密码不一致',
    en: 'Passwords do not match',
  },
  errorNicknameRequired: {
    zh: '请输入昵称',
    en: 'Nickname is required',
  },
  errorPlayerIdRequired: {
    zh: '请输入玩家ID',
    en: 'Player ID is required',
  },
  errorPlayerIdInvalid: {
    zh: '请输入有效的玩家ID',
    en: 'Please enter a valid Player ID',
  },
  errorCodeRequired: {
    zh: '请输入验证码',
    en: 'Verification code is required',
  },
  errorLoginFailed: {
    zh: '登录失败，请检查用户名和密码',
    en: 'Login failed, please check your credentials',
  },
  errorRegisterFailed: {
    zh: '注册失败',
    en: 'Registration failed',
  },
}

registerTranslations('auth', authTranslations)

/**
 * Auth Module - 认证模块入口
 * 包含用户注册、登录、成员绑定等功能
 */

// 引入翻译配置
import './config/translation'

// 初始化认证拦截器 - 自动刷新 JWT Token
import { setupAuthInterceptor } from './interceptors/authInterceptor'
setupAuthInterceptor()

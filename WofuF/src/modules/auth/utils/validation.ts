/**
 * 验证码生成工具
 * 用于生成玩家绑定验证码
 */

import CryptoJS from 'crypto-js'

/**
 * 生成成员绑定验证码
 * 规则: SHA-256(playerId:today:secretKey) 取前6位
 * 
 * @param playerId 玩家ID
 * @param secretKey 密钥
 * @returns 6位验证码
 */
export function generateMemberCode(playerId: string, secretKey: string): string {
  // 获取今天零点的时间字符串 (与后端保持一致)
  const today = getTodayDateString()
  const data = `${playerId}:${today}:${secretKey}`
  
  // SHA-256 哈希
  const hash = CryptoJS.SHA256(data)
  const hashWords = hash.words
  
  // 取前两个字节转成6位数字
  const byte0 = ((hashWords[0] ?? 0) >>> 24) & 0xFF
  const byte1 = ((hashWords[0] ?? 0) >>> 16) & 0xFF
  
  const code = byte0.toString().padStart(3, '0') + byte1.toString().padStart(3, '0')
  return code.slice(0, 6)
}

/**
 * 获取今天的日期字符串 (YYYY-MM-DD 格式)
 */
function getTodayDateString(): string {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}T00:00`
}

/**
 * 验证玩家ID格式 (UUID)
 */
export function isValidPlayerId(playerId: string): boolean {
  const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i
  return uuidRegex.test(playerId)
}

/**
 * 验证昵称格式
 * 规则: 3-50字符，只允许 a-zA-Z0-9_-
 */
export function isValidNickname(nickname: string): { valid: boolean; message: string } {
  if (nickname.length < 3) {
    return { valid: false, message: '昵称至少需要3个字符' }
  }
  if (nickname.length > 50) {
    return { valid: false, message: '昵称最多50个字符' }
  }
  if (!/^[a-zA-Z0-9_-]+$/.test(nickname)) {
    return { valid: false, message: '昵称只能包含字母、数字、下划线和连字符' }
  }
  return { valid: true, message: '' }
}

/**
 * 验证用户名格式
 * 规则: 3-50字符，只允许 a-zA-Z0-9_-
 */
export function isValidUsername(username: string): { valid: boolean; message: string } {
  if (username.length < 3) {
    return { valid: false, message: '用户名至少需要3个字符' }
  }
  if (username.length > 50) {
    return { valid: false, message: '用户名最多50个字符' }
  }
  if (!/^[a-zA-Z0-9_-]+$/.test(username)) {
    return { valid: false, message: '用户名只能包含字母、数字、下划线和连字符' }
  }
  return { valid: true, message: '' }
}

/**
 * 验证邮箱格式
 */
export function isValidEmail(email: string): { valid: boolean; message: string } {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(email)) {
    return { valid: false, message: '请输入有效的邮箱地址' }
  }
  return { valid: true, message: '' }
}

/**
 * 验证密码格式
 * 规则: 6-100字符
 */
export function isValidPassword(password: string): { valid: boolean; message: string } {
  if (password.length < 6) {
    return { valid: false, message: '密码至少需要6个字符' }
  }
  if (password.length > 100) {
    return { valid: false, message: '密码最多100个字符' }
  }
  return { valid: true, message: '' }
}

/**
 * JWT 工具类 - 解析和检查 JWT Token
 */

/**
 * JWT Payload 结构
 */
interface JwtPayload {
  sub?: string
  iat?: number
  exp?: number
  uid?: string
  jti?: string
  ver?: string
  [key: string]: unknown
}

/**
 * 解析 JWT Token 的 Payload 部分（不验证签名）
 */
export function parseJwtPayload(token: string): JwtPayload | null {
  try {
    const parts = token.split('.')
    if (parts.length !== 3) {
      return null
    }

    const payload = parts[1]
    if (!payload) {
      return null
    }

    const decoded = atob(payload.replace(/-/g, '+').replace(/_/g, '/'))
    return JSON.parse(decoded)
  } catch {
    return null
  }
}

/**
 * 获取 JWT Token 的过期时间（毫秒时间戳）
 */
export function getTokenExpiration(token: string): number | null {
  const payload = parseJwtPayload(token)
  if (!payload || payload.exp === undefined) {
    return null
  }
  // exp 是秒级时间戳，转换为毫秒
  return payload.exp * 1000
}

/**
 * 检查 Token 是否已过期
 * @param token JWT Token
 * @param bufferSeconds 提前多少秒视为过期（默认 60 秒）
 */
export function isTokenExpired(token: string, bufferSeconds = 60): boolean {
  const expiration = getTokenExpiration(token)
  if (!expiration) {
    return true
  }

  const now = Date.now()
  const bufferMs = bufferSeconds * 1000

  // 如果 Token 将在 bufferSeconds 内过期，视为已过期
  return now + bufferMs >= expiration
}

/**
 * 获取 Token 剩余有效时间（毫秒）
 */
export function getTokenRemainingTime(token: string): number {
  const expiration = getTokenExpiration(token)
  if (!expiration) {
    return 0
  }

  const remaining = expiration - Date.now()
  return Math.max(0, remaining)
}

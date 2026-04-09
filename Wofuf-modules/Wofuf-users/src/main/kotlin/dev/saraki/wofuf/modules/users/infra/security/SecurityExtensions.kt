package dev.saraki.wofuf.modules.users.infra.security

import org.springframework.security.core.context.SecurityContextHolder

/**
 * 获取当前登录用户的 userId
 * @throws IllegalStateException 如果用户未登录
 */
fun requireCurrentUserId(): String {
    val authentication = SecurityContextHolder.getContext().authentication
    return authentication?.principal as? String
        ?: throw IllegalStateException("用户未登录")
}

/**
 * 获取当前登录用户的 userId（可空）
 * @return 当前用户的 userId，未登录返回 null
 */
fun getCurrentUserId(): String? {
    val authentication = SecurityContextHolder.getContext().authentication
    return authentication?.principal as? String
}

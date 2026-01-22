package dev.saraki.wofuf.modules.users.services.auth

import dev.saraki.wofuf.modules.users.domain.AuthSession
import dev.saraki.wofuf.modules.users.domain.JwtClaims
import dev.saraki.wofuf.modules.users.domain.JwtToken
import dev.saraki.wofuf.modules.users.domain.RefreshToken
import dev.saraki.wofuf.modules.users.domain.User

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/16 13:32
 *   @description:
 */

interface IAuth {

    /**
     * 用户登录
     * 创建新的会话（JWT + Refresh Token）
     */
    fun login(user: User): AuthSession

    /**
     * 鉴权
     * 校验 JWT 是否仍然是一个有效会话
     */
    fun authenticate(jwtToken: JwtToken): JwtClaims?

    /**
     * 使用 Refresh Token 刷新会话（Rotation）
     */
    fun refresh(refreshToken: RefreshToken): AuthSession?

    /**
     * 注销当前会话（单点退出）
     */
    fun logout(jwtToken: JwtToken)

    /**
     * 强制用户全部会话失效（改密码 / 封号）
     */
    fun forceLogoutAll(userId: String)
}
package dev.saraki.wofuf.modules.forum.services.auth

import dev.saraki.wofuf.modules.users.domain.valueObjects.JwtClaims
import dev.saraki.wofuf.modules.users.domain.valueObjects.JwtToken

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/16 13:32
 *   @description:
 */

interface UserAuthService {
    /**
     * 鉴权
     * 校验 JWT 是否仍然是一个有效会话
     */
    fun authenticate(jwtToken: JwtToken): JwtClaims?

    fun signJWT(jwtClaims: JwtClaims): JwtToken
    /**
     * 解码 JWT 令牌
     */
    fun decodeJWT(jwtToken: JwtToken): JwtClaims?
}
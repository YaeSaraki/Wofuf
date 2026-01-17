package dev.saraki.wofuf.modules.users.services

import dev.saraki.wofuf.modules.users.domain.JwtClaims
import dev.saraki.wofuf.modules.users.domain.JwtToken
import dev.saraki.wofuf.modules.users.domain.RefreshToken
import dev.saraki.wofuf.modules.users.repos.persistence.UserEntity

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/16 13:32
 *   @description:
 */

interface IAuthService {
    fun signJWT(jwtClaims: JwtClaims): JwtToken
    fun decodeJWT(jwtToken: JwtToken): JwtClaims?
    fun createRefreshToken(): RefreshToken
    fun getTokens(username: String): List<JwtToken>?
    fun saveAuthenticatedUser(user: UserEntity): Unit?
    fun deAuthenticateUser(user: UserEntity): Unit?
    fun refreshTokenExists(username: String): Result<Boolean>?
    fun getUserNameFromRefreshToken(refreshToken: RefreshToken): String?
}
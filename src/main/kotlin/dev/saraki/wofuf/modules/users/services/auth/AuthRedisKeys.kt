package dev.saraki.wofuf.modules.users.services.auth

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/19 00:35
 *   @description:
 */
object AuthRedisKeys {
    fun session(jti: String) = "auth:session:$jti"
    fun userSessions(userId: String) = "auth:user:sessions:$userId"
    fun tokenVersion(userId: String) = "auth:user:tokenVersion:$userId"
    fun refresh(refreshToken: String) = "auth:refresh:$refreshToken"
}
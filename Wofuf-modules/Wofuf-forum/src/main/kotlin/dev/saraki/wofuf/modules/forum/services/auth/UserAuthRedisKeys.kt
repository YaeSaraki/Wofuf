package dev.saraki.wofuf.modules.forum.services.auth

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/19 00:35
 *   @description:
 */
object UserAuthRedisKeys {
    fun session(jti: String) = "auth:session:$jti"
    fun userSessions(userId: String) = "auth:user:sessions:$userId"
    fun tokenVersion(tokenVersion: String) = "auth:user:tokenVersion:$tokenVersion"
    fun refresh(refreshToken: String) = "auth:refresh:$refreshToken"
}
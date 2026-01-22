package dev.saraki.wofuf.modules.users.services.auth

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/19 00:35
 *   @description:
 */
@ConfigurationProperties(prefix = "auth.jwt")
data class AuthProperties(
    val secret: String,
    val accessExpiryMs: Long,
    val refreshExpiryMs: Long
)
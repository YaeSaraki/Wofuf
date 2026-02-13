package dev.saraki.wofuf.auth.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "auth.jwt")
data class JwtConfig(
    /**
     * 密钥（至少32位）
     */
    var secret: String = "your-secret-key-must-be-at-least-32-bytes-long-and-secure!",

    /**
     * 访问令牌过期时间（毫秒）默认1小时
     */
    var expiration: Long = 3600000,

    /**
     * 刷新令牌过期时间（毫秒）默认7天
     */
    var refreshExpiration: Long = 604800000,

    /**
     * 令牌颁发者
     */
    var issuer: String = "Wofuf-infra-auth",

    /**
     * 允许的时钟偏差（秒）
     */
    var clockSkew: Int = 60,

    /**
     * 版本
     * 初始版本为0
     */
    var tokenVersion: String = "0"
)
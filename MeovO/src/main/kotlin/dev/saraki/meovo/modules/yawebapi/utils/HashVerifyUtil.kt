package dev.saraki.meovo.modules.yawebapi.utils

import dev.saraki.meovo.modules.yawebapi.config.YaWebApiConfig
import java.security.MessageDigest
import java.time.LocalDate
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.util.Base64
import java.util.UUID

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/11 18:20
 *   @description:
 */
object HashVerifyUtil {
    private val SECRET_KEY = YaWebApiConfig.getSecretKey
    
    fun generateCode(string: String): String {
        val today = LocalDate.now().atStartOfDay().toString()
        val data = "$string:$today:$SECRET_KEY"
        val hash = MessageDigest.getInstance("SHA-256").digest(data.toByteArray())
        // 取前4字节转成6位数字
        val code = (hash[0].toInt() and 0xFF).toString().padStart(3, '0') +
                   (hash[1].toInt() and 0xFF).toString().padStart(3, '0')
        return code.take(6)
    }
    
    /**
     * 生成签名（完整验证）
     */
    fun generateToken(uniqueId: UUID): String {
        val timestamp = System.currentTimeMillis() / 1000 / 60 // 分钟级，允许时间误差
        val data = "$uniqueId:$timestamp"
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(SECRET_KEY.toByteArray(), "HmacSHA256"))
        val hash = mac.doFinal(data.toByteArray())
        return "$timestamp.${Base64.getUrlEncoder().withoutPadding().encodeToString(hash).take(16)}"
    }
    
    /**
     * 验证签名
     */
    fun verifyToken(uniqueId: UUID, token: String): Boolean {
        val parts = token.split(".")
        if (parts.size != 2) return false
        val timestamp = parts[0].toLongOrNull() ?: return false
        val current = System.currentTimeMillis() / 1000 / 60
        // 只允许10分钟误差
        if (kotlin.math.abs(current - timestamp) > 10) return false
        
        val data = "$uniqueId:$timestamp"
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(SECRET_KEY.toByteArray(), "HmacSHA256"))
        val hash = mac.doFinal(data.toByteArray())
        val expected = Base64.getUrlEncoder().withoutPadding().encodeToString(hash).take(16)
        return expected == parts[1]
    }
}
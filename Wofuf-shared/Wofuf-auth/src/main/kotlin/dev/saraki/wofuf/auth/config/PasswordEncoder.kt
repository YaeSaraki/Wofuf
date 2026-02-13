package dev.saraki.wofuf.auth.config

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/16 14:47
 *   @description:
 */

class PasswordEncoder: PasswordEncoder {
    private val bCryptPasswordEncoder = BCryptPasswordEncoder()

    /**
     * 对原始密码进行加密
     * @param rawPassword 原始密码
     * @return 加密后的密码
     */
    override fun encode(rawPassword: CharSequence?): String? {
        return bCryptPasswordEncoder.encode(rawPassword)
    }

    /**
     * 验证原始密码是否与加密后的密码匹配
     * @param rawPassword 原始密码
     * @param encodedPassword 加密后的密码
     * @return 如果匹配则返回true，否则返回false
     */
    override fun matches(rawPassword: CharSequence?, encodedPassword: String?): Boolean {
        return bCryptPasswordEncoder.matches(rawPassword, encodedPassword)
    }
}
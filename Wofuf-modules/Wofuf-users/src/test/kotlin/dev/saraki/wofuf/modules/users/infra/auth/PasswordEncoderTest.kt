package dev.saraki.wofuf.modules.users.infra.auth

import dev.saraki.wofuf.auth.config.PasswordEncoder
import kotlin.test.Test
import kotlin.test.*
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import kotlin.test.DefaultAsserter.assertNotNull

class PasswordEncoderTest {

    @Test
    fun passwordEncoderTest() {
        val rawPassword = "123456"
        val passwordEncoder = PasswordEncoder()
        val encodedPassword = passwordEncoder.encode(rawPassword)
        // Kotlin 自带断言（更简洁）
        assertNotNull(encodedPassword, "加密密码不能为空")
        assertNotEquals(rawPassword, encodedPassword)
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword))
        println("rawPassword: $rawPassword, encodedPassword: $encodedPassword")
    }

    @Test
    fun bCryptPasswordEncoderTest() {
        val rawPassword = "123456"
        val encoder = BCryptPasswordEncoder()
        val encoded = encoder.encode(rawPassword)
        assertNotNull(encoded)
        assertTrue(encoder.matches(rawPassword, encoded))
    }
}
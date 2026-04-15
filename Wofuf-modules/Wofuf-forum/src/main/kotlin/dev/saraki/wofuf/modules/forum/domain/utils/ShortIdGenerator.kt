package dev.saraki.wofuf.modules.forum.domain.utils

import java.security.MessageDigest

/**
 * 短 ID 生成器
 * 使用 UUID 的哈希值生成 8 位短 ID
 */
object ShortIdGenerator {
    private const val BASE62_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
    private const val SHORT_ID_LENGTH = 8

    /**
     * 从 UUID 生成短 ID
     * 使用 UUID 的 MD5 哈希，然后取前 8 个字符
     */
    fun generateFromUuid(uuid: String): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(uuid.toByteArray())
        return encodeToBase62(digest).take(SHORT_ID_LENGTH)
    }

    /**
     * 从任意字符串生成短 ID
     */
    fun generateFromString(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(input.toByteArray())
        return encodeToBase62(digest).take(SHORT_ID_LENGTH)
    }

    /**
     * 将字节数组编码为 Base62 字符串
     */
    private fun encodeToBase62(bytes: ByteArray): String {
        val sb = StringBuilder()
        var i = 0
        // 取前 10 字节，足以生成 8 位短 ID
        while (i < minOf(10, bytes.size)) {
            val b = bytes[i].toInt() and 0xFF
            sb.append(BASE62_CHARS[b % BASE62_CHARS.length])
            i++
        }
        return sb.toString()
    }
}

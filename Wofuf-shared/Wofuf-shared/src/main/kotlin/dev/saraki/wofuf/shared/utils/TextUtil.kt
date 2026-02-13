package dev.saraki.wofuf.shared.utils

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 21:32
 *   @description:
 */
object TextUtil {
    private val EMAIL_REGEX = Regex(
        "^(([^<>()\\[\\]\\\\.,;:\\s@\"]+(\\.[^<>()\\[\\]\\\\.,;:\\s@\"]+)*)|(\".+\"))@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$"
    )

    /**
     * 判断字符串是否为空或只包含空格
     */
    fun isBlank(text: String?): Boolean {
        return text.isNullOrBlank()
    }

    /**
     * 判断字符串是否不为空且不只包含空格
     */
    fun isNotBlank(text: String?): Boolean {
        return !isBlank(text)
    }

    /**
     * 对字符串进行trim操作，移除首尾空格
     */
    fun sanitize(text: String?): String? {
        return text?.trim()
    }

    /**
     * 验证邮箱地址是否符合格式
     */
    fun validateEmailAddress(email: String?): Boolean {
        if (email.isNullOrEmpty()) return false
        return EMAIL_REGEX.matches(email.lowercase())
    }

    /**
     * 创建一个指定长度的随机数字字符串
     */
    fun createRandomNumericString(numberDigits: Int): String {
        val chars = ('0'..'9').toList()
        return (1..numberDigits)
            .map { chars.random() }
            .joinToString("")
    }
}
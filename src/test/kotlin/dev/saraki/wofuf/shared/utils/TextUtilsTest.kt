package dev.saraki.wofuf.shared.utils

import kotlin.test.Test

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 21:41
 *   @description:
 */
class TextUtilsTest {
    @Test
    fun createRandomNumericStringTest() {
        val randomString = TextUtil.createRandomNumericString(6)
        assert(randomString.length == 6)
        assert(randomString.all { it.isDigit() })
    }
}
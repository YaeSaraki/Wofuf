package dev.saraki.wofuf.shared.utils

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/20 17:51
 *   @description:
 */
object StringUtil {
    // 工具方法：提取为顶层函数，复用性更高
   fun String.toListFromString(separator: String = ","): List<String> {
        return if (this.isBlank()) {
            emptyList()
        } else {
            this.split(separator)
                .map { it.trim() } // 去除首尾空格，避免空字符串元素
                .filter { it.isNotBlank() } // 过滤空元素
                .toList()
        }
    }

   fun List<String>.toStringForStore(separator: String = ","): String {
        return this.joinToString(separator)
    }
}
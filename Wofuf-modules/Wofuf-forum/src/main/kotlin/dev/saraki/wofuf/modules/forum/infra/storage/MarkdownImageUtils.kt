package dev.saraki.wofuf.modules.forum.infra.storage

/**
 * Markdown 图片工具类
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 */
object MarkdownImageUtils {

    // 每个Post最多允许的图片数量
    const val MAX_IMAGES_PER_POST = 9

    // Markdown图片语法正则: ![alt](url)
    private val IMAGE_REGEX = Regex("""!\[.*?\]\((.*?)\)""")

    /**
     * 从Markdown文本中提取所有图片URL
     * @param markdown Markdown文本
     * @return 图片URL列表
     */
    fun extractImageUrls(markdown: String?): List<String> {
        if (markdown.isNullOrBlank()) return emptyList()
        return IMAGE_REGEX.findAll(markdown)
            .map { it.groupValues[1] }
            .toList()
    }

    /**
     * 统计Markdown文本中的图片数量
     * @param markdown Markdown文本
     * @return 图片数量
     */
    fun countImages(markdown: String?): Int {
        if (markdown.isNullOrBlank()) return 0
        return IMAGE_REGEX.findAll(markdown).count()
    }

    /**
     * 验证图片数量是否超过限制
     * @param markdown Markdown文本
     * @return Pair<是否有效, 实际图片数量>
     */
    fun validateImageCount(markdown: String?): Pair<Boolean, Int> {
        val count = countImages(markdown)
        return Pair(count <= MAX_IMAGES_PER_POST, count)
    }
}

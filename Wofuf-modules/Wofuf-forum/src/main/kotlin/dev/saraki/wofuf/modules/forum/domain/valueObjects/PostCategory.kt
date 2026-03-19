package dev.saraki.wofuf.modules.forum.domain.valueObjects

/**
 * 帖子分类枚举
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 */
enum class PostCategory {
    DISCUSSION,    // 讨论
    SHARE,         // 分享
    QUESTION,      // 求助
    ANNOUNCEMENT;  // 公告

    companion object {
        fun fromString(value: String): PostCategory {
            return valueOf(value.uppercase())
        }
    }
}

package dev.saraki.wofuf.modules.forum.domain.valueObjects

/**
 * 帖子分类枚举
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 */
enum class PostCategory {
    DISCUSSION,    // 讨论 (兼容旧数据)
    QUESTION,      // 问答
    SHOWCASE,      // 展示 (原 SHARE)
    NEWS,          // 新闻
    GUIDE,         // 教程
    SHARE,         // 分享 (兼容旧数据，映射到 SHOWCASE)
    ANNOUNCEMENT,  // 公告 (兼容旧数据，映射到 NEWS)
    ;

    companion object {
        fun fromString(value: String): PostCategory {
            return try {
                valueOf(value.uppercase())
            } catch (e: IllegalArgumentException) {
                DISCUSSION
            }
        }
    }
}

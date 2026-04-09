package dev.saraki.wofuf.modules.forum.domain.valueObjects

/**
 * 帖子状态枚举
 * 用于管理帖子的可见性和审核状态
 *
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 * @date 2026/4/8
 */
enum class PostStatus {
    NORMAL,      // 正常显示
    HIDDEN,      // 已隐藏
    UNDER_REVIEW // 审核中
}

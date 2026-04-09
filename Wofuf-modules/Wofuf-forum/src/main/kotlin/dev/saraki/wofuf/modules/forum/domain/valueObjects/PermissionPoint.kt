package dev.saraki.wofuf.modules.forum.domain.valueObjects

/**
 * 权限点枚举
 * 用于论坛管理功能的细粒度权限控制
 *
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 * @date 2026/4/8
 */
enum class PermissionPoint {
    // 帖子管理权限
    POST_PIN,           // 置顶帖子
    POST_FEATURE,       // 加精帖子
    POST_HIDE,          // 隐藏帖子
    POST_REVIEW,        // 审核帖子
    POST_DELETE_ANY,    // 删除任意帖子

    // 评论管理权限
    COMMENT_DELETE_ANY, // 删除任意评论
    COMMENT_VIEW_HIDDEN, // 查看隐藏评论

    // 分类/标签管理权限
    CATEGORY_MANAGE,    // 管理分类

    // 用户管理权限
    USER_BAN,           // 封禁用户
    USER_VIEW_BANNED,   // 查看封禁用户

    // 系统管理权限
    ADMIN_ACCESS,       // 管理后台访问
    PERMISSION_GRANT    // 授予权限
}

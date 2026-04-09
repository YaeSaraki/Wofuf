package dev.saraki.wofuf.modules.forum.infra.exception

/**
 * 权限拒绝异常
 * 当用户没有所需权限时抛出
 *
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 * @date 2026/4/8
 */
class PermissionDeniedException(
    message: String = "权限不足"
) : RuntimeException(message)

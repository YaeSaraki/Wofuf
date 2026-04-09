package dev.saraki.wofuf.modules.forum.infra.annotation

import dev.saraki.wofuf.modules.forum.domain.valueObjects.PermissionPoint
import kotlin.reflect.KClass

/**
 * 权限验证注解
 * 用于标记需要特定权限才能访问的方法
 *
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 * @date 2026/4/8
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequirePermission(
    val permission: PermissionPoint,
    val message: String = "权限不足"
)

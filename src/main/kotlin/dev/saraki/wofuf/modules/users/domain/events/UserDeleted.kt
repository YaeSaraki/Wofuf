package dev.saraki.wofuf.modules.users.domain.events

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 23:16
 *   @description:
 */

import dev.saraki.wofuf.modules.users.domain.UserId
import java.time.LocalDateTime

class UserDeleted(
    val userId: UserId,
    val deletedAt: LocalDateTime
) {
    // 可以在这里添加事件处理逻辑
}

package dev.saraki.wofuf.modules.users.domain.events

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 23:15
 *   @description:
 */
import dev.saraki.wofuf.modules.users.domain.UserEmail
import dev.saraki.wofuf.modules.users.domain.UserId
import java.time.LocalDateTime

class EmailVerified(
    val userId: UserId,
    val email: UserEmail,
    val verifiedAt: LocalDateTime
) {
    // 可以在这里添加事件处理逻辑
}
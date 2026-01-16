package dev.saraki.wofuf.modules.users.domain.events

import dev.saraki.wofuf.modules.users.domain.UserEmail
import dev.saraki.wofuf.modules.users.domain.UserId
import java.time.LocalDateTime

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 23:16
 *   @description:
 */
class UserLoggedIn(id: UserId, email: UserEmail, now: LocalDateTime) {
}
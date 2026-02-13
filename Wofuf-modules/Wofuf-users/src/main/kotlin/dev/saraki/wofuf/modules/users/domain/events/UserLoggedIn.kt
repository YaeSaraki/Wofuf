package dev.saraki.wofuf.modules.users.domain.events

import dev.saraki.wofuf.modules.users.domain.User
import dev.saraki.wofuf.shared.domain.events.IDomainEvent

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 23:16
 *   @description:
 */
class UserLoggedIn(
    user: User,
) : IDomainEvent(user.userId.value)
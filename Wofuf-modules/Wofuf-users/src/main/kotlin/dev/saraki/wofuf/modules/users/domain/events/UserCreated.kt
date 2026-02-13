package dev.saraki.wofuf.modules.users.domain.events

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 23:16
 *   @description:
 */
import dev.saraki.wofuf.modules.users.domain.User
import dev.saraki.wofuf.shared.domain.events.IDomainEvent

class UserCreated (
    val user: User
) : IDomainEvent(user.userId.value)

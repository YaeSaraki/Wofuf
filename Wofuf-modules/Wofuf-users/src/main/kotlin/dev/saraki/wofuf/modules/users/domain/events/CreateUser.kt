package dev.saraki.wofuf.modules.users.domain.events

import dev.saraki.wofuf.shared.domain.UniqueEntityId
import dev.saraki.wofuf.shared.domain.events.IDomainEvent

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/20 17:58
 *   @description:
 */
class CreateUser (
    val userId: String,
    val username: String,
    val password: String
) : IDomainEvent(UniqueEntityId(userId))
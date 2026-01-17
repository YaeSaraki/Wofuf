package dev.saraki.wofuf.modules.users.domain.events

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 23:16
 *   @description:
 */

import dev.saraki.wofuf.modules.users.domain.User
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import dev.saraki.wofuf.shared.domain.events.IDomainEvent
import java.time.LocalDateTime

class UserDeleted(
    val user: User,
    override val dataTimeOccurred: LocalDateTime,
    private val aggregateId: UniqueEntityId = UniqueEntityId(),
): IDomainEvent {
    // 可以在这里添加事件处理逻辑
    override fun getAggregateId(): UniqueEntityId {
        return aggregateId
    }
}

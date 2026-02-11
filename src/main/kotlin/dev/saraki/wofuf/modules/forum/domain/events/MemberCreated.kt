package dev.saraki.wofuf.modules.forum.domain.events

import dev.saraki.wofuf.modules.forum.domain.Member
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import dev.saraki.wofuf.shared.domain.events.IDomainEvent
import java.time.LocalDateTime

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/11 11:44
 *   @description:
 */
class MemberCreated(
    val member: Member,
    override val dataTimeOccurred: LocalDateTime
) : IDomainEvent {
    override fun getAggregateId(): UniqueEntityId {
        return getAggregateId()
    }
}
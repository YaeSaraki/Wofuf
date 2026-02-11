package dev.saraki.wofuf.modules.users.domain.events

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 23:15
 *   @description:
 */
import dev.saraki.wofuf.modules.users.domain.UserEmail
import dev.saraki.wofuf.modules.users.domain.UserId
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import dev.saraki.wofuf.shared.domain.events.IDomainEvent
import java.time.LocalDateTime

class EmailVerified(
    val userId: UserId,
    val email: UserEmail,
    val verifiedAt: LocalDateTime,
    override val dataTimeOccurred: LocalDateTime
) : IDomainEvent {
    // 可以在这里添加事件处理逻辑
    override fun getAggregateId(): UniqueEntityId {
        return getAggregateId()
    }
}
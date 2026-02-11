package dev.saraki.wofuf.modules.forum.domain.events

import dev.saraki.wofuf.modules.forum.domain.Comment
import dev.saraki.wofuf.modules.forum.domain.Post
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import dev.saraki.wofuf.shared.domain.events.IDomainEvent
import java.time.LocalDateTime

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/11 11:41
 *   @description:
 */
class CommentPosted(
    val post: Post,
    val comment : Comment,
    override val dataTimeOccurred: LocalDateTime
) : IDomainEvent {
    override fun getAggregateId(): UniqueEntityId {
        return getAggregateId()
    }
}
package dev.saraki.wofuf.modules.forum.domain.events

import dev.saraki.wofuf.modules.forum.domain.Post
import dev.saraki.wofuf.modules.forum.domain.PostVote
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import dev.saraki.wofuf.shared.domain.events.IDomainEvent
import java.time.LocalDateTime

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/11 11:45
 *   @description:
 */
class PostVotesChanged(
    val post: Post,
    val vote: PostVote,
    override val dataTimeOccurred: LocalDateTime
) : IDomainEvent {
    override fun getAggregateId(): UniqueEntityId {
        return getAggregateId()
    }
}
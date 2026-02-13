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
    val comment: Comment,
    val post: Post,
) : IDomainEvent(comment.commentId.value)
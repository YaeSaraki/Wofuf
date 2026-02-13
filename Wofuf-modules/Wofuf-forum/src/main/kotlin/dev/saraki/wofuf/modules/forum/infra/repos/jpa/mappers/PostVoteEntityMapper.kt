package dev.saraki.wofuf.modules.forum.infra.repos.jpa.mappers

import dev.saraki.wofuf.modules.forum.domain.*
import dev.saraki.wofuf.modules.forum.infra.repos.jpa.entities.PostVoteEntity
import dev.saraki.wofuf.shared.domain.UniqueEntityId

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/8 17:29
 *   @description:
 */
object PostVoteEntityMapper {

    fun toDomain(entity: PostVoteEntity): PostVote {
        val postVoteOrError = PostVote.create(
            props = PostVoteProps(
                postId = PostId.create(UniqueEntityId(entity.postId)).getOrThrow(),
                memberId = MemberId.create(UniqueEntityId(entity.memberId)).getOrThrow(),
                type = VoteType.valueOf(entity.voteType)
            ),
            id = UniqueEntityId(entity.voteId)
        )

        val postVote = postVoteOrError.getOrThrow()

        postVote._createdAt = entity.createdAt
        postVote._updatedAt = entity.updatedAt

        return postVote
    }

    fun toEntity(domain: PostVote): PostVoteEntity {
        return PostVoteEntity(
            voteId = domain._id.uuid.toString(),
            postId = domain.postId.stringValue,
            memberId = domain.memberId.stringValue,
            voteType = domain.type.toString()
        )
    }
}
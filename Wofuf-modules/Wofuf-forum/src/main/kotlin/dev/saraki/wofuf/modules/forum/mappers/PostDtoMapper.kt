package dev.saraki.wofuf.modules.forum.mappers

import dev.saraki.wofuf.modules.forum.domain.Post
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberDetails
import dev.saraki.wofuf.modules.forum.dtos.MemberDto
import dev.saraki.wofuf.modules.forum.dtos.PostDto

/**
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 * @date 2026/3/15
 * @description Mapper for converting Post domain objects to PostDto
 */
object PostDtoMapper {
    fun toDto(post: Post, memberDetails: MemberDetails, numComments: Int, points: Int): PostDto =
        PostDto(
            postId = post.postId.stringValue,
            slug = post.slug.value,
            title = post.title.value,
            createdAt = post.dateTimePosted,
            memberPostBy = MemberDto(
                nickname = memberDetails.nickName.value,
                reputation = memberDetails.reputation,
                playerId = memberDetails.playerId?.stringValue
            ),
            numComments = numComments,
            points = points,
            text = post.text?.value ?: "",
            link = post.link?.value ?: "",
            type = post.type,
            category = post.category,
            status = post.status,
            isPinned = post.isPinned,
            isFeatured = post.isFeatured,
            wasUpvotedByMe = null,
            wasDownvotedByMe = null
        )

    fun toDtoWithVoteStatus(
        post: Post,
        memberDetails: MemberDetails,
        numComments: Int,
        points: Int,
        wasUpvotedByMe: Boolean = false,
        wasDownvotedByMe: Boolean = false
    ): PostDto =
        PostDto(
            postId = post.postId.stringValue,
            slug = post.slug.value,
            title = post.title.value,
            createdAt = post.dateTimePosted,
            memberPostBy = MemberDto(
                nickname = memberDetails.nickName.value,
                reputation = memberDetails.reputation,
                playerId = memberDetails.playerId?.stringValue
            ),
            numComments = numComments,
            points = points,
            text = post.text?.value ?: "",
            link = post.link?.value ?: "",
            type = post.type,
            category = post.category,
            status = post.status,
            isPinned = post.isPinned,
            isFeatured = post.isFeatured,
            wasUpvotedByMe = wasUpvotedByMe,
            wasDownvotedByMe = wasDownvotedByMe
        )
}

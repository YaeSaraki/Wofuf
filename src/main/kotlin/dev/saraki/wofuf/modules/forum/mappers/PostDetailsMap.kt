package dev.saraki.wofuf.modules.forum.mappers

import dev.saraki.wofuf.modules.forum.domain.PostDetails
import dev.saraki.wofuf.modules.forum.dtos.PostDto

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/9 13:30
 *   @description:
 */
object PostDetailsMap {
    fun from(postDetails: PostDetails): PostDto {
        return PostDto(
            slug = postDetails.slug.value,
            title = postDetails.title.value,
            createdAt = postDetails.dateTimePosted,
            memberPostBy = MemberDetailsMap.from(postDetails.member),
            numComments = postDetails.numComments ?: 0,
            points = postDetails.points,
            text = postDetails.text?.value ?: "",
            link = postDetails.link?.value ?: "",
            type = postDetails.type,
            wasDownvotedByMe = postDetails.wasDownvotedByMe,
            wasUpvotedByMe = postDetails.wasUpvotedByMe,
        )
    }
}
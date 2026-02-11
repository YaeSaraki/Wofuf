package dev.saraki.wofuf.modules.forum.infra.repos.jpa.mappers

import dev.saraki.wofuf.modules.forum.domain.MemberDetails
import dev.saraki.wofuf.modules.forum.domain.Post
import dev.saraki.wofuf.modules.forum.domain.PostDetails
import dev.saraki.wofuf.modules.forum.domain.PostDetailsProps

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/9 21:24
 *   @description:
 */
object PostDetailsMapper {
    fun toDomain(post: Post, memberDetails: MemberDetails): PostDetails {
        return PostDetails.create(
            PostDetailsProps(
                member = memberDetails,
                slug = post.slug,
                title = post.title,
                type = post.type,
                text = post.text,
                link = post.link,
                numComments = post.totalNumComments,
                points = post.points,
                dateTimePosted = post.dateTimePosted,
                wasUpvotedByMe = null,
                wasDownvotedByMe = null,
            )

        ).getOrThrow()
    }
}
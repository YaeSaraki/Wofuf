package dev.saraki.wofuf.modules.forum.domain.valueObjects

import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberDetails
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostSlug
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostTitle
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostType
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostText
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostLink
import dev.saraki.wofuf.shared.core.Guard
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.domain.ValueObject
import java.time.LocalDateTime

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/3/15 13:00
 *   @description:
 */
data class PostDetailsProps(
    val member: MemberDetails,
    val slug: PostSlug,
    val title: PostTitle,
    val type: PostType,
    val text: PostText?,
    val link: PostLink?,
    val numComments: Int,
    val points: Int,
    val dateTimePosted: LocalDateTime,
    val wasUpvotedByMe: Boolean,
    val wasDownvotedByMe: Boolean
)

class PostDetails private constructor(
    props: PostDetailsProps
) : ValueObject<PostDetailsProps>(props) {
    val member: MemberDetails
        get() = props.member

    val slug: PostSlug
        get() = props.slug

    val title: PostTitle
        get() = props.title

    val type: PostType
        get() = props.type

    val text: PostText?
        get() = props.text

    val link: PostLink?
        get() = props.link

    val numComments: Int
        get() = props.numComments

    val points: Int
        get() = props.points

    val dateTimePosted: LocalDateTime
        get() = props.dateTimePosted

    val wasUpvotedByMe: Boolean
        get() = props.wasUpvotedByMe

    val wasDownvotedByMe: Boolean
        get() = props.wasDownvotedByMe

    companion object {
        fun create(props: PostDetailsProps): Result<PostDetails> {
            val guardResult = Guard.againstNullOrUndefinedBulk(
                listOf(
                    Guard.GuardArgument(props.member, "member"),
                    Guard.GuardArgument(props.slug, "slug"),
                    Guard.GuardArgument(props.title, "title"),
                    Guard.GuardArgument(props.type, "type"),
                    Guard.GuardArgument(props.numComments, "numComments"),
                    Guard.GuardArgument(props.points, "points"),
                    Guard.GuardArgument(props.dateTimePosted, "dateTimePosted"),
                    Guard.GuardArgument(props.wasUpvotedByMe, "wasUpvotedByMe"),
                    Guard.GuardArgument(props.wasDownvotedByMe, "wasDownvotedByMe"),
                )
            )
            if (guardResult.isFailure) {
                return Result.failure(guardResult.exceptionOrThrow())
            }
            return Result.success(PostDetails(props))
        }
    }
}

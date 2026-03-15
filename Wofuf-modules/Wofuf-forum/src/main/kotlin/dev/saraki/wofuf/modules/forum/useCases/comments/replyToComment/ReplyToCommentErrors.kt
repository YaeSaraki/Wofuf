package dev.saraki.wofuf.modules.forum.useCases.comments.replyToComment

import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCaseError

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/3/15 16:15
 *   @description:
 */
class ReplyToCommentErrors {

    // Post slug is empty
    class PostSlugEmptyError() : Result.Failure<ReplyToCommentDto.Response>(
        exception = UseCaseError(
            code = "POST_SLUG_EMPTY",
            message = "Post slug cannot be empty"
        )
    )

    // Post not found
    class PostNotFoundError(val slug: String) : Result.Failure<ReplyToCommentDto.Response>(
        exception = UseCaseError(
            code = "POST_NOT_FOUND",
            message = "Couldn't find a post by slug {$slug}"
        )
    )

    // Comment not found
    class CommentNotFoundError(val commentId: String) : Result.Failure<ReplyToCommentDto.Response>(
        exception = UseCaseError(
            code = "COMMENT_NOT_FOUND",
            message = "Couldn't find a comment by commentId {$commentId}"
        )
    )

    // Member not found
    class MemberNotFoundError(val userId: String) : Result.Failure<ReplyToCommentDto.Response>(
        exception = UseCaseError(
            code = "MEMBER_NOT_FOUND",
            message = "Couldn't find a member by userId {$userId}"
        )
    )

    // Comment text is empty
    class CommentTextEmptyError() : Result.Failure<ReplyToCommentDto.Response>(
        exception = UseCaseError(
            code = "COMMENT_TEXT_EMPTY",
            message = "Comment text cannot be empty"
        )
    )
}

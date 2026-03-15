package dev.saraki.wofuf.modules.forum.useCases.comments.replyToPost

import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCaseError

class ReplyToPostErrors {

    // Comment text is empty
    class CommentTextEmptyError() : Result.Failure<ReplyToPostDto.Response>(
        exception = UseCaseError(
            code = "COMMENT_TEXT_EMPTY",
            message = "Comment text cannot be empty"
        )
    )

    // Post not found
    class PostNotFoundError(val postId: String) : Result.Failure<ReplyToPostDto.Response>(
        exception = UseCaseError(
            code = "POST_NOT_FOUND",
            message = "Couldn't find a post by postId {$postId}"
        )
    )

    // Member not found
    class MemberNotFoundError(val userId: String) : Result.Failure<ReplyToPostDto.Response>(
        exception = UseCaseError(
            code = "MEMBER_NOT_FOUND",
            message = "Couldn't find a member by userId {$userId}"
        )
    )
}

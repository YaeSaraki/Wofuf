package dev.saraki.wofuf.modules.forum.useCases.comments.downvoteComment

import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCaseError

class DownvoteCommentErrors {

    // Comment ID is empty
    class CommentIdEmptyError : Result.Failure<DownvoteCommentDto.Response>(
        exception = UseCaseError(
            code = "COMMENT_ID_EMPTY",
            message = "Comment ID cannot be empty"
        )
    )

    // User ID is empty
    class UserIdEmptyError : Result.Failure<DownvoteCommentDto.Response>(
        exception = UseCaseError(
            code = "USER_ID_EMPTY",
            message = "User ID cannot be empty"
        )
    )

    // Comment not found
    class CommentNotFoundError(val commentId: String) : Result.Failure<DownvoteCommentDto.Response>(
        exception = UseCaseError(
            code = "COMMENT_NOT_FOUND",
            message = "Couldn't find a comment by commentId {$commentId}"
        )
    )

    // Member not found
    class MemberNotFoundError(val userId: String) : Result.Failure<DownvoteCommentDto.Response>(
        exception = UseCaseError(
            code = "MEMBER_NOT_FOUND",
            message = "Couldn't find a member by userId {$userId}"
        )
    )

    // Already downvoted
    class AlreadyDownvotedError(val commentId: String, val userId: String) : Result.Failure<DownvoteCommentDto.Response>(
        exception = UseCaseError(
            code = "ALREADY_DOWNVOTED",
            message = "User {$userId} has already downvoted comment {$commentId}"
        )
    )

    // Downvote failed
    class DownvoteFailedError(val commentId: String) : Result.Failure<DownvoteCommentDto.Response>(
        exception = UseCaseError(
            code = "DOWNVOTE_FAILED",
            message = "Failed to downvote comment {$commentId}"
        )
    )
}

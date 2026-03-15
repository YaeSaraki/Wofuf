package dev.saraki.wofuf.modules.forum.useCases.comments.upvoteComment

import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCaseError

class UpvoteCommentErrors {

    // Comment ID is empty
    class CommentIdEmptyError() : Result.Failure<UpvoteCommentDto.Response>(
        exception = UseCaseError(
            code = "COMMENT_ID_EMPTY",
            message = "Comment ID cannot be empty"
        )
    )

    // User ID is empty
    class UserIdEmptyError() : Result.Failure<UpvoteCommentDto.Response>(
        exception = UseCaseError(
            code = "USER_ID_EMPTY",
            message = "User ID cannot be empty"
        )
    )

    // Comment not found
    class CommentNotFoundError(val commentId: String) : Result.Failure<UpvoteCommentDto.Response>(
        exception = UseCaseError(
            code = "COMMENT_NOT_FOUND",
            message = "Couldn't find a comment by commentId {$commentId}"
        )
    )

    // Member not found
    class MemberNotFoundError(val userId: String) : Result.Failure<UpvoteCommentDto.Response>(
        exception = UseCaseError(
            code = "MEMBER_NOT_FOUND",
            message = "Couldn't find a member by userId {$userId}"
        )
    )

    // Already upvoted
    class AlreadyUpvotedError(val commentId: String, val userId: String) : Result.Failure<UpvoteCommentDto.Response>(
        exception = UseCaseError(
            code = "ALREADY_UPVOTED",
            message = "User {$userId} has already upvoted comment {$commentId}"
        )
    )

    // Upvote failed
    class UpvoteFailedError(val commentId: String) : Result.Failure<UpvoteCommentDto.Response>(
        exception = UseCaseError(
            code = "UPVOTE_FAILED",
            message = "Failed to upvote comment {$commentId}"
        )
    )
}

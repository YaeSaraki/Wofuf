package dev.saraki.wofuf.modules.forum.useCases.members.deleteOwnComment

import dev.saraki.wofuf.modules.forum.useCases.members.deleteOwnComment.DeleteOwnCommentDto
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCaseError

class DeleteOwnCommentErrors {
    class CommentIdEmptyError : Result.Failure<DeleteOwnCommentDto.Response>(
        exception = UseCaseError(code = "COMMENT_ID_EMPTY", message = "Comment ID cannot be empty")
    )

    class UnauthorizedError : Result.Failure<DeleteOwnCommentDto.Response>(
        exception = UseCaseError(code = "UNAUTHORIZED", message = "User is not logged in")
    )

    class MemberNotFoundError : Result.Failure<DeleteOwnCommentDto.Response>(
        exception = UseCaseError(code = "MEMBER_NOT_FOUND", message = "Member not found")
    )

    class CommentNotFoundOrNotOwnedError : Result.Failure<DeleteOwnCommentDto.Response>(
        exception = UseCaseError(code = "COMMENT_NOT_FOUND_OR_NOT_OWNED", message = "Comment not found or you don't own this comment")
    )
}

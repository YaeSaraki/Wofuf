package dev.saraki.wofuf.modules.forum.useCases.comments.getCommentByCommentId

import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCaseError

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/3/14 21:00
 *   @description:
 */
class GetCommentByCommentIdErrors {

    // Comment not found
    class CommentNotFoundError() : Result.Failure<GetCommentByCommentIdDto.Response>(
        exception = UseCaseError(
            code = "COMMENT_NOT_FOUND",
            message = "Comment not found"
        )
    )
}

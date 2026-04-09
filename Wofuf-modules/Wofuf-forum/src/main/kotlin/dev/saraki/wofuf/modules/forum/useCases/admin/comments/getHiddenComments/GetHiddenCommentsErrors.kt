package dev.saraki.wofuf.modules.forum.useCases.admin.comments.getHiddenComments

import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCaseError

class GetHiddenCommentsErrors {
    class InvalidPageError : Result.Failure<GetHiddenCommentsDto.Response>(
        exception = UseCaseError(code = "INVALID_PAGE_ERROR", message = "Page must be non-negative")
    )

    class InvalidSizeError : Result.Failure<GetHiddenCommentsDto.Response>(
        exception = UseCaseError(code = "INVALID_SIZE_ERROR", message = "Size must be between 1 and 100")
    )
}

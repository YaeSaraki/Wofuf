package dev.saraki.wofuf.modules.forum.useCases.posts.editPost

import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostTitle
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCaseError

/**
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 * @date 2026/3/15
 * @description Error classes for edit post use case
 */
class EditPostErrors {

    /** Post ID is empty */
    class PostIdEmptyError : Result.Failure<EditPostDto.Response>(
        exception = UseCaseError(
            code = "POST_ID_EMPTY_ERROR",
            message = "Post ID cannot be empty"
        )
    )

    /** Post not found */
    class PostNotFoundError(val postId: String) : Result.Failure<EditPostDto.Response>(
        exception = UseCaseError(
            code = "POST_NOT_FOUND_ERROR",
            message = "Couldn't find a post by postId {$postId}"
        )
    )

    /** No fields provided for update */
    class NoUpdateDataError : Result.Failure<EditPostDto.Response>(
        exception = UseCaseError(
            code = "NO_UPDATE_DATA_ERROR",
            message = "At least one field (title, text, or link) must be provided for update"
        )
    )

    /** Invalid title */
    class InvalidTitleError(val title: String) : Result.Failure<EditPostDto.Response>(
        exception = UseCaseError(
            code = "INVALID_TITLE_ERROR",
            message = "Invalid title: '$title'. Title must be between ${PostTitle.MIN_LENGTH} and ${PostTitle.MAX_LENGTH} characters"
        )
    )

    /** Invalid text */
    class InvalidTextError(val reason: String) : Result.Failure<EditPostDto.Response>(
        exception = UseCaseError(
            code = "INVALID_TEXT_ERROR",
            message = "Invalid text: $reason"
        )
    )

    /** Invalid link */
    class InvalidLinkError(val link: String) : Result.Failure<EditPostDto.Response>(
        exception = UseCaseError(
            code = "INVALID_LINK_ERROR",
            message = "Invalid link: '$link'"
        )
    )

    /** Update failed */
    class UpdateFailedError(val postId: String) : Result.Failure<EditPostDto.Response>(
        exception = UseCaseError(
            code = "UPDATE_FAILED_ERROR",
            message = "Failed to update post with postId {$postId}"
        )
    )

    /** Too many images */
    class TooManyImagesError(val count: Int, val max: Int) : Result.Failure<EditPostDto.Response>(
        exception = UseCaseError(
            code = "TOO_MANY_IMAGES_ERROR",
            message = "图片数量超出限制: 最多允许 $max 张图片，当前有 $count 张"
        )
    )

    /** User not authenticated */
    class UnauthorizedError : Result.Failure<EditPostDto.Response>(
        exception = UseCaseError(
            code = "UNAUTHORIZED_ERROR",
            message = "用户未登录"
        )
    )

    /** Member not found */
    class MemberNotFoundError : Result.Failure<EditPostDto.Response>(
        exception = UseCaseError(
            code = "MEMBER_NOT_FOUND_ERROR",
            message = "用户信息不存在"
        )
    )

    /** Forbidden - not the author */
    class ForbiddenError : Result.Failure<EditPostDto.Response>(
        exception = UseCaseError(
            code = "FORBIDDEN_ERROR",
            message = "无权编辑此帖子"
        )
    )
}

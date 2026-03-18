package dev.saraki.wofuf.modules.forum.useCases.posts.createPost

import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCaseError

class CreatePostErrors {

    // User ID is empty
    class UserIdEmptyError() : Result.Failure<CreatePostDto.Response>(
        exception = UseCaseError(
            code = "USER_ID_EMPTY",
            message = "User ID cannot be empty"
        )
    )

    // Title is empty
    class TitleEmptyError() : Result.Failure<CreatePostDto.Response>(
        exception = UseCaseError(
            code = "TITLE_EMPTY",
            message = "Title cannot be empty"
        )
    )

    // Type is invalid
    class TypeInvalidError(val type: String) : Result.Failure<CreatePostDto.Response>(
        exception = UseCaseError(
            code = "TYPE_INVALID",
            message = "Invalid post type: {$type}"
        )
    )

    // Member not found
    class MemberNotFoundError(val userId: String) : Result.Failure<CreatePostDto.Response>(
        exception = UseCaseError(
            code = "MEMBER_NOT_FOUND",
            message = "Couldn't find a member by userId {$userId}"
        )
    )

    // Post creation failed
    class PostCreationFailedError() : Result.Failure<CreatePostDto.Response>(
        exception = UseCaseError(
            code = "POST_CREATION_FAILED",
            message = "Failed to create post"
        )
    )

    // Too many images
    class TooManyImagesError(val count: Int, val max: Int) : Result.Failure<CreatePostDto.Response>(
        exception = UseCaseError(
            code = "TOO_MANY_IMAGES",
            message = "图片数量超出限制: 最多允许 $max 张图片，当前有 $count 张"
        )
    )
}

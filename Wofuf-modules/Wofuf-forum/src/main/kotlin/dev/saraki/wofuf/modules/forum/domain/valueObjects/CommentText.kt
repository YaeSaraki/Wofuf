package dev.saraki.wofuf.modules.forum.domain.valueObjects

import dev.saraki.wofuf.shared.core.Guard
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.domain.ValueObject

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/6 12:47
 *   @description: CommentText value object
 */

data class CommentTextProps(
    val value: String
)

class CommentText private constructor(
    props: CommentTextProps
) : ValueObject<CommentTextProps>(props) {

    val value: String
        get() = props.value

    companion object {
        const val MIN_LENGTH: Int = 2
        const val MAX_LENGTH: Int = 20000

        fun create(value: String): Result<CommentText> {
            val guardResult = Guard.againstNullOrUndefined(value, "CommentText")
            if (guardResult.isFailure) {
                return Result.failure(guardResult.getOrThrow())
            }

            val minGuardResult = Guard.againstAtLeast(MIN_LENGTH, value)
            if (minGuardResult.isFailure) {
                return Result.failure(minGuardResult.exceptionOrThrow())
            }

            val maxGuardResult = Guard.againstAtMost(MAX_LENGTH, value)
            if (maxGuardResult.isFailure) {
                return Result.failure(maxGuardResult.exceptionOrThrow())
            }

            return Result.success(CommentText(CommentTextProps(value)))
        }
    }
}

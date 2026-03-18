package dev.saraki.wofuf.modules.forum.domain.valueObjects

import dev.saraki.wofuf.shared.core.Guard
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.domain.ValueObject

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/8 14:05
 *   @description: PostText value object
 */

data class PostTextProps(
    val value: String
)

class PostText private constructor(
    props: PostTextProps
) : ValueObject<PostTextProps>(props) {

    val value: String
        get() = props.value

    companion object {
        const val MIN_LENGTH: Int = 2
        const val MAX_LENGTH: Int = 50000

        fun create(value: String): Result<PostText> {
            val guardResult = Guard.againstNullOrUndefined(value, "PostText")
            if (guardResult.isFailure) {
                return Result.failure(guardResult.exceptionOrThrow())
            }

            val minGuardResult = Guard.againstAtLeast(MIN_LENGTH, value)
            if (minGuardResult.isFailure) {
                return Result.failure(minGuardResult.exceptionOrThrow())
            }

            val maxGuardResult = Guard.againstAtMost(MAX_LENGTH, value)
            if (maxGuardResult.isFailure) {
                return Result.failure(maxGuardResult.exceptionOrThrow())
            }

            return Result.success(PostText(PostTextProps(value)))
        }
    }
}

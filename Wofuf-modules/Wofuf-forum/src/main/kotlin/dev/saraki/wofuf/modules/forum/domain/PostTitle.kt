package dev.saraki.wofuf.modules.forum.domain

import dev.saraki.wofuf.shared.core.Guard
import dev.saraki.wofuf.shared.domain.ValueObject

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/6 22:56
 *   @description:
 */
data class PostTitleProps(
    val value: String
)

class PostTitle private constructor(
    props: PostTitleProps
) : ValueObject<PostTitleProps>(props) {

    val value: String
        get() = props.value

    companion object {
        const val MIN_LENGTH: Int = 2
        const val MAX_LENGTH: Int = 100

        fun create(value: String): Result<PostTitle> {
            val guardResult = Guard.againstNullOrUndefined(value, "PostTitle")
            val lengthGuardResult = Guard.againstAtMost(MIN_LENGTH, "PostTitle is too short")
            val maxLengthGuardResult = Guard.againstAtLeast(MAX_LENGTH, "PostTitle is too long")

            val combinedGuardResult = Guard.combine(listOf(guardResult, lengthGuardResult, maxLengthGuardResult))
            if (combinedGuardResult.isFailure) {
                return Result.failure(combinedGuardResult.exceptionOrThrow())
            }

            return Result.success(PostTitle(PostTitleProps(value)))
        }
    }
}

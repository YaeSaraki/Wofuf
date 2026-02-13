package dev.saraki.wofuf.modules.forum.domain

import dev.saraki.wofuf.shared.core.Guard
import dev.saraki.wofuf.shared.domain.ValueObject

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/8 14:05
 *   @description:
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
        fun create(value: String): Result<PostText> {
            val nullGuard = Guard.againstNullOrUndefinedBulk(
                listOf(
                    Guard.GuardArgument(value, "PostText")
                )
            )
            if (nullGuard.isFailure) {
                return Result.failure(nullGuard.exceptionOrThrow())
            }
            return Result.success(PostText(PostTextProps(value)))
        }
    }
}
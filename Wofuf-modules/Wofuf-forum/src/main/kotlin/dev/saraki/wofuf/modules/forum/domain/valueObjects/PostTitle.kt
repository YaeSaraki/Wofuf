package dev.saraki.wofuf.modules.forum.domain.valueObjects

import dev.saraki.wofuf.shared.core.AppError
import dev.saraki.wofuf.shared.core.Guard
import dev.saraki.wofuf.shared.core.Result
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
            if (guardResult.isFailure) {
                return Result.failure(guardResult.exceptionOrThrow())
            }
            
            // 检查最小长度 - 文本长度必须至少为 MIN_LENGTH
            val minLengthResult = Guard.againstAtLeast(MIN_LENGTH, value)
            if (minLengthResult.isFailure) {
                return Result.failure(AppError("PostTitle must be at least $MIN_LENGTH characters", "TITLE_TOO_SHORT"))
            }
            
            // 检查最大长度 - 文本长度必须最多为 MAX_LENGTH
            val maxLengthResult = Guard.againstAtMost(MAX_LENGTH, value)
            if (maxLengthResult.isFailure) {
                return Result.failure(AppError("PostTitle must be at most $MAX_LENGTH characters", "TITLE_TOO_LONG"))
            }

            return Result.success(PostTitle(PostTitleProps(value)))
        }
    }
}

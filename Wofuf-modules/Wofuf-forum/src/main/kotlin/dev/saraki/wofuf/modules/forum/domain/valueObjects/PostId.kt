package dev.saraki.wofuf.modules.forum.domain.valueObjects

import dev.saraki.wofuf.shared.core.Guard
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import dev.saraki.wofuf.shared.domain.ValueObject

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/6 12:56
 *   @description:
 */
data class PostIdProps(
    val value: UniqueEntityId
)

class PostId private constructor(
    props: PostIdProps
) : ValueObject<PostIdProps>(props) {
    val value: UniqueEntityId
        get() = props.value

    val stringValue: String
        get() = props.value.uuid.toString()

    companion object {
        fun create(value: UniqueEntityId): Result<PostId> {
            val guardResult = Guard.againstNullOrUndefined(value, "PostId")
            if (guardResult.isFailure) {
                return Result.failure(guardResult.getOrThrow())
            }
            return Result.success(PostId(PostIdProps(value)))
        }
    }
}
package dev.saraki.wofuf.modules.forum.domain.valueObjects

import dev.saraki.wofuf.shared.core.Guard
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import dev.saraki.wofuf.shared.domain.ValueObject

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/6 13:09
 *   @description:
 */

data class CommentIdProps(
    val value: UniqueEntityId
)

class CommentId private constructor(
    props: CommentIdProps
) : ValueObject<CommentIdProps>(props) {
    val value: UniqueEntityId
        get() = props.value

    val stringValue: String
        get() = value.uuid.toString()

    companion object {
        fun create(value: UniqueEntityId): Result<CommentId> {
            val guardResult = Guard.againstNullOrUndefined(value, "CommentId")
            if (guardResult.isFailure) {
                return Result.failure(guardResult.getOrThrow())
            }
            return Result.success(CommentId(CommentIdProps(value)))
        }
    }
}
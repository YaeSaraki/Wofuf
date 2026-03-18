package dev.saraki.wofuf.modules.forum.domain.valueObjects

import dev.saraki.wofuf.shared.core.Guard
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import dev.saraki.wofuf.shared.domain.ValueObject

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/3/18
 *   @description: CommentVoteId value object
 */

data class CommentVoteIdProps(
    val value: UniqueEntityId
)

class CommentVoteId private constructor(
    props: CommentVoteIdProps
) : ValueObject<CommentVoteIdProps>(props) {

    val value: UniqueEntityId
        get() = props.value

    val stringValue: String
        get() = props.value.uuid.toString()

    companion object {
        fun create(value: UniqueEntityId): Result<CommentVoteId> {
            val guardResult = Guard.againstNullOrUndefined(value, "CommentVoteId")
            if (guardResult.isFailure) {
                return Result.failure(guardResult.getOrThrow())
            }
            return Result.success(CommentVoteId(CommentVoteIdProps(value)))
        }
    }
}

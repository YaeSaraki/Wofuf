package dev.saraki.wofuf.modules.forum.domain

import dev.saraki.wofuf.shared.core.Guard
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import dev.saraki.wofuf.shared.domain.ValueObject

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/6 12:41
 *   @description:
 */
data class MemberIdProps(
    val value: UniqueEntityId
)

class MemberId private constructor(
    props: MemberIdProps
): ValueObject<MemberIdProps>(props) {
    val value: UniqueEntityId
        get() = props.value
    companion object {
        fun create(value: UniqueEntityId): Result<MemberId> {
            val guardResult = Guard.againstNullOrUndefined(value, "MemberId")
            if (guardResult.isFailure) {
                return Result.failure(guardResult.getOrThrow())
            }
            return Result.success(MemberId(MemberIdProps(value)))
        }
    }
}

package dev.saraki.wofuf.modules.forum.domain.valueObjects

import dev.saraki.wofuf.shared.core.Guard
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.domain.ValueObject

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/3/15 12:50
 *   @description:
 */
data class MemberDetailsProps(
    val nickName: NickName,
    val reputation: Int,
)

class MemberDetails private constructor(
    props: MemberDetailsProps
) : ValueObject<MemberDetailsProps>(props) {
    val nickName: NickName
        get() = props.nickName

    val reputation: Int
        get() = props.reputation

    companion object {
        fun create(props: MemberDetailsProps): Result<MemberDetails> {
            val guardResult = Guard.againstNullOrUndefinedBulk(
                listOf(
                    Guard.GuardArgument(props.nickName, "nickName"),
                    Guard.GuardArgument(props.reputation, "reputation"),
                )
            )
            if (guardResult.isFailure) {
                return Result.failure(guardResult.exceptionOrThrow())
            }
            return Result.success(MemberDetails(props))
        }
    }
}
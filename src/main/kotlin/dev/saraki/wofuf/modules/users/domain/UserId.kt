package dev.saraki.wofuf.modules.users.domain

import dev.saraki.wofuf.shared.core.Guard
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import dev.saraki.wofuf.shared.domain.ValueObject

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 23:16
 *   @description: 用户身份值对象
 */
data class UserIdProps(val value: UniqueEntityId)

class UserId private constructor(
    props: UserIdProps
) : ValueObject<UserIdProps>(props) {
    val value: UniqueEntityId get() = props.value
    val stringValue: String
        get() = value.uuid.toString()

    companion object {
        fun create(value: UniqueEntityId): Result<UserId> {
            val guardResult = Guard.againstNullOrUndefined(value, "UserId")
            if (guardResult.isFailure) {
                return Result.failure(guardResult.getOrThrow())
            }
            return Result.success(UserId(UserIdProps(value)))
        }
    }
}
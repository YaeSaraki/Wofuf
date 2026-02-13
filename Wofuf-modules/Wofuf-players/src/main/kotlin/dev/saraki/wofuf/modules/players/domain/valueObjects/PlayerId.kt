package dev.saraki.wofuf.modules.players.domain.valueObjects

import dev.saraki.wofuf.shared.core.Guard
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import dev.saraki.wofuf.shared.domain.ValueObject

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/19 15:21
 *   @description:
 */

data class PlayerIdProps(
    val value: UniqueEntityId
)

class PlayerId private constructor(
    props: PlayerIdProps
) : ValueObject<PlayerIdProps>(props) {
    val stringValue: String
        get() = props.value.uuid.toString()

    companion object {
        fun create(value: UniqueEntityId): Result<PlayerId> {
            val guardResult = Guard.againstNullOrUndefined(value, "PlayerId")
            if (guardResult.isFailure) {
                return Result.failure(guardResult.getOrThrow())
            }
            return Result.success(PlayerId(PlayerIdProps(value)))
        }
    }
}
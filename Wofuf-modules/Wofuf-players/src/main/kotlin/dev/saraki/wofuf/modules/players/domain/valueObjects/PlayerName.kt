package dev.saraki.wofuf.modules.players.domain.valueObjects

import dev.saraki.wofuf.shared.core.Guard
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.domain.ValueObject

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/19 15:21
 *   @description:
 */

data class PlayerNameProps(
    val value: String
)

class PlayerName private constructor(
    props: PlayerNameProps
) : ValueObject<PlayerNameProps>(props) {
    val stringValue: String
        get() = props.value

    companion object {
        fun create(value: String): Result<PlayerName> {
            val guardResult = Guard.againstNullOrUndefined(value, "PlayerName")
            if (guardResult.isFailure) {
                return Result.failure(guardResult.getOrThrow())
            }
            return Result.success(PlayerName(PlayerNameProps(value)))
        }

        val UNKNOWN: PlayerName
            get() = PlayerName(PlayerNameProps("UNKNOWN"))
    }
}
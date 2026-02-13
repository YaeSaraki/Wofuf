package dev.saraki.wofuf.modules.players.domain.valueObjects

import com.fasterxml.jackson.annotation.JsonValue
import dev.saraki.wofuf.shared.core.Guard
import dev.saraki.wofuf.shared.domain.ValueObject

data class PlayerStatisticProps(
    val key: String,
    val category: String,
    val value: Long
)

class PlayerStatistic private constructor(
    props: PlayerStatisticProps
) : ValueObject<PlayerStatisticProps>(props) {

    val key: String
        get() = props.key

    val category: String
        get() = props.category

    val value: Long
        get() = props.value

    companion object {
        fun create(props: PlayerStatisticProps): Result<PlayerStatistic> {
            val guardResult = Guard.againstNullOrUndefined(props.key, "PlayerStatistic.key")
            if (guardResult.isFailure) {
                return Result.failure(guardResult.exceptionOrThrow())
            }
            return Result.success(PlayerStatistic(props))
        }

        val defaultProps: PlayerStatistic = PlayerStatistic(
            PlayerStatisticProps(
                key = "default",
                category = "default",
                value = 0
            )
        )
    }
}

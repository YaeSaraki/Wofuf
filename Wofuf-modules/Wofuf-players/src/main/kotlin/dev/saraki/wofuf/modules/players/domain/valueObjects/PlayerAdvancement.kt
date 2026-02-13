package dev.saraki.wofuf.modules.players.domain.valueObjects

import dev.saraki.wofuf.shared.core.Guard
import dev.saraki.wofuf.shared.domain.ValueObject

data class PlayerAdvancementProps(
    val key: String,
    val done: Boolean,
    val completed: List<String>,
    val remaining: List<String>
)

class PlayerAdvancement private constructor(
    props: PlayerAdvancementProps
) : ValueObject<PlayerAdvancementProps>(props) {

    val key: String
        get() = props.key

    val done: Boolean
        get() = props.done

    val completed: List<String>
        get() = props.completed

    val remaining: List<String>
        get() = props.remaining

    companion object {
        fun create(props: PlayerAdvancementProps): Result<PlayerAdvancement> {
            val guardResult = Guard.againstNullOrUndefinedBulk(
                listOf(
                    Guard.GuardArgument(props.key, "PlayerAdvancement.key"),
                )
            )
            if (guardResult.isFailure) {
                return Result.failure(guardResult.exceptionOrThrow())
            }
            return Result.success(PlayerAdvancement(props))
        }

        val defaultProps: PlayerAdvancement = PlayerAdvancement( PlayerAdvancementProps(
            key = "default",
            done = false,
            completed = emptyList(),
            remaining = emptyList()
        ))
    }
}
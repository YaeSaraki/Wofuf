package dev.saraki.wofuf.modules.players.domain

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import dev.saraki.wofuf.shared.core.Guard
import dev.saraki.wofuf.shared.domain.ValueObject

data class PlayerAdvancementProps(
    val key: String,
    val done: Boolean,
    val completed: List<String>,
    val remaining: List<String>
)

class PlayerAdvancement private constructor(
    override val props: PlayerAdvancementProps
) : ValueObject<PlayerAdvancementProps>(props) {

    val key: String
        get() = props.key

    val done: Boolean
        get() = props.done

    val completed: List<String>
        get() = props.completed

    val remaining: List<String>
        get() = props.remaining

    @JsonValue
    fun asProps(): PlayerAdvancementProps {
        return props
    }

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

        @JsonCreator
        fun fromJson(
            @JsonProperty("key") key: String,
            @JsonProperty("done") done: Boolean,
            @JsonProperty("completed") completed: List<String> = emptyList(),
            @JsonProperty("remaining") remaining: List<String> = emptyList()
        ): PlayerAdvancement {
            return PlayerAdvancement(PlayerAdvancementProps(key, done, completed, remaining))
        }
    }
}
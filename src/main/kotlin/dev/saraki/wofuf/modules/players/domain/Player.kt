package dev.saraki.wofuf.modules.players.domain

import dev.saraki.wofuf.shared.domain.AggregateRoot
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.Guard
import dev.saraki.wofuf.shared.domain.UniqueEntityId

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/19 15:42
 *   @description:
 */
data class PlayerProps(
    val name: String,
    val firstLogin: Long,
    val lastLogin: Long,
    val totalPlaytimeSeconds: Long,
    val updateTime: Long,
    val statistics: Map<String, PlayerStatistic>,
    val advancements: Map<String, PlayerAdvancement>,
    val playerSkin: PlayerSkin
)

class Player private constructor(
    props: PlayerProps,
    id: UniqueEntityId? = null,
): AggregateRoot<PlayerProps>(props, id) {
    val playerId: PlayerId
        get() = PlayerId.create(_id).getOrThrow()

    val playerName: String
        get() = props.name

    val firstLogin: Long
        get() = props.firstLogin

    val lastLogin: Long
        get() = props.lastLogin

    val totalPlaytimeSeconds: Long
        get() = props.totalPlaytimeSeconds

    val updateTime: Long
        get() = props.updateTime

    val advancements: Map<String, PlayerAdvancement>
        get() = props.advancements

    val statistics: Map<String, PlayerStatistic>
        get() = props.statistics

    val playerSkin: PlayerSkin
        get() = props.playerSkin

    fun updateProps(props: PlayerProps) : Result<Player> {
        return create(props, id)
    }

    companion object {
        fun create(props: PlayerProps, id: UniqueEntityId? = null) : Result<Player> {
            val guardResult = Guard.againstNullOrUndefinedBulk(
                listOf(
                    Guard.GuardArgument(props.name, "Player name cannot be null or blank")
                )
            )
            if (guardResult.isFailure) {
                return Result.failure(guardResult.exceptionOrThrow())
            }

            val defaultProps = props.copy(
                firstLogin = props.firstLogin,
                lastLogin = props.lastLogin,
                totalPlaytimeSeconds = props.totalPlaytimeSeconds,
                updateTime = props.updateTime,
                statistics = props.statistics,
                advancements = props.advancements,
                playerSkin = props.playerSkin,
            )

            val player = Player(defaultProps, id)

            return Result.success(player)
        }

    }


}

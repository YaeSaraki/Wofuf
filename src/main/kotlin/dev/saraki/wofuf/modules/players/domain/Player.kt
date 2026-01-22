package dev.saraki.wofuf.modules.players.domain

import dev.saraki.wofuf.shared.domain.AggregateRoot
import dev.saraki.wofuf.shared.core.Result

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/19 15:42
 *   @description:
 */
data class Player(
    val id : PlayerId,
    val props: PlayerProps,
    val statistics: Map<String, PlayerStatistic>,
    val advancements: Map<String, PlayerAdvancement>
): AggregateRoot<Player>() {
    companion object {
        fun create(id: PlayerId, props: PlayerProps, statistics: Map<String, PlayerStatistic>, advancements: Map<String, PlayerAdvancement>) : Result<Player> = Result.success(
            Player(
                id = id,
                props = props,
                statistics = statistics,
                advancements = advancements
            )
        )
    }

    fun updateProps(props: PlayerProps) : Result<Player> = Result.success(
        copy(props = props)
    )

    fun updateStatistics(statistics: Map<String, PlayerStatistic>) : Result<Player> = Result.success(
        copy(statistics = statistics)
    )

    fun updateAdvancements(advancements: Map<String, PlayerAdvancement>) : Result<Player> = Result.success(
        copy(advancements = advancements)
    )

}

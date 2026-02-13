package dev.saraki.wofuf.modules.players.useCases.collectPlayerData

import dev.saraki.wofuf.modules.players.domain.valueObjects.PlayerAdvancement
import dev.saraki.wofuf.modules.players.domain.valueObjects.PlayerSkin
import dev.saraki.wofuf.modules.players.domain.valueObjects.PlayerStatistic

data class CollectPlayerDataCommand(
    val uuid: String,
    val name: String,
    val firstLogin: Long,
    val lastLogin: Long,
    val totalPlaytimeSeconds: Long,
    val statistics: Map<String, PlayerStatistic>,
    val advancements: Map<String, PlayerAdvancement>,
    val playerSkin: PlayerSkin
)

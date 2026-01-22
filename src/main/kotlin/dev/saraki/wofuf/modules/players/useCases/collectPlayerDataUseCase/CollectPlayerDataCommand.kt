package dev.saraki.wofuf.modules.players.useCases.collectPlayerDataUseCase

import dev.saraki.wofuf.modules.players.domain.PlayerAdvancement
import dev.saraki.wofuf.modules.players.domain.PlayerStatistic

data class CollectPlayerDataCommand(
    val uuid: String,
    val name: String,
    val firstLogin: Long,
    val lastLogin: Long,
    val totalPlaytimeSeconds: Long,
    val statistics: Map<String, PlayerStatistic>,
    val advancements: Map<String, PlayerAdvancement>
)

package dev.saraki.wofuf.modules.players.useCases.collectPlayerData.alc

import dev.saraki.wofuf.modules.players.domain.valueObjects.PlayerStatisticProps

data class StatisticResult(
    val uuid: String,
    val name: String?,
    val statistics: Map<String, PlayerStatisticProps>
)
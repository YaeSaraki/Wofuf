package dev.saraki.wofuf.modules.players.useCases.collectPlayerDataUseCase.alc

import dev.saraki.wofuf.modules.players.domain.PlayerStatistic

data class StatisticResult(
    val uuid: String,
    val name: String?,
    val statistics: Map<String, PlayerStatistic>
)
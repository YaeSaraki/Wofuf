package dev.saraki.wofuf.modules.players.useCases.collectPlayerDataUseCase.alc

interface PluginApiClient {

    fun fetchOnlinePlayers(): List<PlayerResult>?

    fun fetchPlayerStatistics(uuid: String): StatisticResult?

    fun fetchPlayerAdvancements(uuid: String): AdvancementResult?
}
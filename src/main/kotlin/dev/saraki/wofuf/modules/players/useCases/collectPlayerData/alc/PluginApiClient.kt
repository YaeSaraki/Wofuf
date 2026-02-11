package dev.saraki.wofuf.modules.players.useCases.collectPlayerData.alc

import java.util.*

interface PluginApiClient {

    fun fetchOnlinePlayers(): List<PlayerResult>?

    fun fetchPlayerStatistics(uuid: UUID): StatisticResult?

    fun fetchPlayerAdvancements(uuid: UUID): AdvancementResult?

    fun fetchPlayerSkin(uuid: UUID): PlayerSkinResult?
}
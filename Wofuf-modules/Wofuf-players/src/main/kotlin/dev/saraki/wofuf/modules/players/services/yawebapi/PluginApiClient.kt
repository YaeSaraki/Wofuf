package dev.saraki.wofuf.modules.players.services.yawebapi

import dev.saraki.wofuf.modules.players.useCases.collectPlayerData.alc.AdvancementResult
import dev.saraki.wofuf.modules.players.useCases.collectPlayerData.alc.PlayerResult
import dev.saraki.wofuf.modules.players.useCases.collectPlayerData.alc.PlayerSkinResult
import dev.saraki.wofuf.modules.players.useCases.collectPlayerData.alc.StatisticResult
import java.util.UUID

interface PluginApiClient {

    fun fetchOnlinePlayers(): List<PlayerResult>?

    fun fetchPlayerStatistics(uuid: UUID): StatisticResult?

    fun fetchPlayerAdvancements(uuid: UUID): AdvancementResult?

    fun fetchPlayerSkin(uuid: UUID): PlayerSkinResult?
}
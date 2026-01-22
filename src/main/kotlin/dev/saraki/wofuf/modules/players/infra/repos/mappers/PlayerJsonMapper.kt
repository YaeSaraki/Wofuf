package dev.saraki.wofuf.modules.players.infra.repos.mappers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.saraki.wofuf.modules.players.domain.PlayerAdvancement
import dev.saraki.wofuf.modules.players.domain.PlayerStatistic

object PlayerJsonMapper {
    private val mapper = ObjectMapper().findAndRegisterModules()

    fun statisticsToJson(stats: Map<String, PlayerStatistic>): String =
        mapper.writeValueAsString(stats)

    fun statisticsFromJson(json: String?): Map<String, PlayerStatistic> =
        json?.let { mapper.readValue(it) } ?: emptyMap()

    fun advancementsToJson(advs: Map<String, PlayerAdvancement>): String =
        mapper.writeValueAsString(advs)

    fun advancementsFromJson(json: String?): Map<String, PlayerAdvancement> =
        json?.let { mapper.readValue(it) } ?: emptyMap()
}
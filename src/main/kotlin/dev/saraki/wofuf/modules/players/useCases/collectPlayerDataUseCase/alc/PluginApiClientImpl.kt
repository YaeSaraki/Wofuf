package dev.saraki.wofuf.modules.players.useCases.collectPlayerDataUseCase.alc

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class PluginApiClientImpl(
    private val restTemplate: RestTemplate,
    @Value("\${plugin.api.base-url}")
    private val baseUrl: String,
) : PluginApiClient {
    private val log = LoggerFactory.getLogger(javaClass)
    override fun fetchOnlinePlayers(): List<PlayerResult>? {
        try {
            val url = "$baseUrl/api/v1/players"
            return restTemplate.getForObject(url, Array<PlayerResult>::class.java)
                ?.toList()
        } catch (e: Exception) {
            log.info("Failed to fetch online players", e)
        }
        return null
    }

    override fun fetchPlayerStatistics(uuid: String): StatisticResult? {
        try {
            val url = "$baseUrl/api/v1/statistics/$uuid"
            return restTemplate.getForObject(url, StatisticResult::class.java)
                ?: throw IllegalStateException("Failed to fetch statistics")
        } catch (e: Exception) {
            log.info("Failed to fetch statistics for player $uuid", e)
        }
        return null
    }

    override fun fetchPlayerAdvancements(uuid: String): AdvancementResult? {
        try {
            val url = "$baseUrl/api/v1/advancements/$uuid"
            return restTemplate.getForObject(url, AdvancementResult::class.java)
                ?: throw IllegalStateException("Failed to fetch advancements")
        } catch (e: Exception) {
            log.info("Failed to fetch advancements for player $uuid", e)
        }
        return null
    }
}
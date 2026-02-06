package dev.saraki.wofuf.modules.players.useCases.collectPlayerDataUseCase.alc

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.util.Base64
import java.util.UUID

@Component
class PluginApiClientImpl(
    private val restTemplate: RestTemplate,
    @Value("\${plugin.api.base-url}")
    private val baseUrl: String,
    @Value("\${plugin.api.backup-skin-url-with-extension-png}")
    private val backupUrl: String,
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

    override fun fetchPlayerStatistics(uuid: UUID): StatisticResult? {
        try {
            val url = "$baseUrl/api/v1/statistics/$uuid"
            return restTemplate.getForObject(url, StatisticResult::class.java)
                ?: throw IllegalStateException("Failed to fetch statistics")
        } catch (e: Exception) {
            log.info("Failed to fetch statistics for player $uuid", e)
        }
        return null
    }

    override fun fetchPlayerAdvancements(uuid: UUID): AdvancementResult? {
        try {
            val url = "$baseUrl/api/v1/advancements/$uuid"
            return restTemplate.getForObject(url, AdvancementResult::class.java)
                ?: throw IllegalStateException("Failed to fetch advancements")
        } catch (e: Exception) {
            log.info("Failed to fetch advancements for player $uuid", e)
        }
        return null
    }

    override fun fetchPlayerSkin(uuid: UUID): PlayerSkinResult? {
        try {
            val url = "$baseUrl/api/v1/skins/$uuid"
            val playerSkinUrl = restTemplate.getForObject(url, SkinUrlResult::class.java)
                ?: throw IllegalStateException("Failed to fetch skin")
            val skinUrl = playerSkinUrl.skin
            val capeUrl = playerSkinUrl.cape
            val type = playerSkinUrl.type

            var skinImg: ByteArray? = null
            var capeImg: ByteArray? = null

            try {
                skinImg = restTemplate.getForObject(skinUrl, ByteArray::class.java)
                    ?: restTemplate.getForObject(backupUrl + ".png", ByteArray::class.java)

                if (capeUrl != "") {
                    capeImg = restTemplate.getForObject(capeUrl, ByteArray::class.java)
                        ?: restTemplate.getForObject(backupUrl + ".png", ByteArray::class.java)
                }

            } catch (
                e: Exception
            ) {
                log.info("Failed to fetch skin for player $uuid", e)
            }

            val skinString = Base64.getEncoder().encodeToString(skinImg ?: ByteArray(0))
            val capeString = Base64.getEncoder().encodeToString(capeImg ?: ByteArray(0))
            return PlayerSkinResult(
                type = type,
                skin = skinString,
                cape = capeString
            )
        } catch (e: Exception) {
            log.info("Failed to fetch skin for player $uuid", e)
        }
        return null
    }
}
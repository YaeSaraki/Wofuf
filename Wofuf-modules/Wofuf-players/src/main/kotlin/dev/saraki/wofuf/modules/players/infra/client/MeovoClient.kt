package dev.saraki.wofuf.modules.players.infra.client

import dev.saraki.wofuf.modules.players.domain.ServerStatus
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/4/8
 *   @description: HTTP client for communicating with Meovo plugin
 */
@Component
class MeovoClient(
    private val restTemplate: RestTemplate,
    @Value("\${plugin.api.base-url}") private val baseUrl: String
) {
    /**
     * Get server status from Meovo plugin
     * @return ServerStatus if successful, null otherwise
     */
    fun getServerStatus(): ServerStatus? {
        return try {
            val url = "$baseUrl/api/v1/server/status"
            @Suppress("UNCHECKED_CAST")
            val response = restTemplate.getForObject(url, Map::class.java)

            if (response != null) {
                ServerStatus.create(
                    onlinePlayers = (response["onlinePlayers"] as? Number)?.toInt() ?: 0,
                    maxPlayers = (response["maxPlayers"] as? Number)?.toInt() ?: 0,
                    tps = (response["tps"] as? Number)?.toDouble() ?: 20.0,
                    heartbeatStatus = response["heartbeatStatus"] as? Boolean ?: true,
                    updateTime = (response["updateTime"] as? Number)?.toLong() ?: System.currentTimeMillis()
                ).getOrThrow()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}

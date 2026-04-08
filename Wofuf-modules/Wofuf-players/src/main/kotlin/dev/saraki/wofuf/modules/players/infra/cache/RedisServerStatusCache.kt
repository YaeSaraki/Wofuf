package dev.saraki.wofuf.modules.players.infra.cache

import com.google.gson.Gson
import dev.saraki.wofuf.modules.players.domain.ServerStatus
import dev.saraki.wofuf.modules.players.services.cache.ServerStatusCache
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/4/8
 *   @description: Redis implementation of ServerStatusCache
 */
@Component
class RedisServerStatusCache(
    @Qualifier("PlayerRedisTemplate")
    private val redisTemplate: RedisTemplate<String, Any>
) : ServerStatusCache {

    private val key = "server:status"
    private val gson = Gson()

    override fun get(): ServerStatus? {
        val value = redisTemplate.opsForValue().get(key) ?: return null
        return try {
            val json = value.toString()
            val data = gson.fromJson(json, Map::class.java)
            ServerStatus.create(
                onlinePlayers = (data["onlinePlayers"] as? Number)?.toInt() ?: 0,
                maxPlayers = (data["maxPlayers"] as? Number)?.toInt() ?: 0,
                tps = (data["tps"] as? Number)?.toDouble() ?: 20.0,
                heartbeatStatus = data["heartbeatStatus"] as? Boolean ?: true,
                updateTime = (data["updateTime"] as? Number)?.toLong() ?: System.currentTimeMillis()
            ).getOrThrow()
        } catch (e: Exception) {
            null
        }
    }

    override fun put(status: ServerStatus) {
        val data = mapOf(
            "onlinePlayers" to status.onlinePlayers,
            "maxPlayers" to status.maxPlayers,
            "tps" to status.tps.doubleValue,
            "heartbeatStatus" to status.heartbeatStatus,
            "updateTime" to status.updateTime
        )
        redisTemplate.opsForValue().set(key, gson.toJson(data), Duration.ofMinutes(5))
    }

    override fun clear() {
        redisTemplate.delete(key)
    }
}

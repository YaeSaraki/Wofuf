package dev.saraki.wofuf.modules.players.infra.cache

import dev.saraki.wofuf.modules.players.services.cache.YesterdayOnlineCache
import dev.saraki.wofuf.modules.players.useCases.getPlayerYesterdayOnline.GetPlayerYesterdayOnlineDto
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RedisYesterdayOnlineStringCache(
    @Qualifier("PlayerRedisTemplate")
    val redisTemplate: RedisTemplate<String, Any>
) : YesterdayOnlineCache {
    private val key = "players:yesterday:online"
    private val opsForValue = redisTemplate.opsForValue()

    override fun get(): GetPlayerYesterdayOnlineDto.Response? {
        val value = opsForValue.get(key) ?: return null
        return GetPlayerYesterdayOnlineDto.Response(value.toString().split(',').map(String::trim))
    }

    override fun put(data: GetPlayerYesterdayOnlineDto.Response) {
        opsForValue.set(
            key,
            data.playerNames.joinToString(", "),
            Duration.ofHours(24)
        )
    }
}
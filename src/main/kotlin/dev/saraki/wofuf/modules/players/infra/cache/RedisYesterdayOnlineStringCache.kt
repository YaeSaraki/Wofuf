package dev.saraki.wofuf.modules.players.infra.cache

import com.fasterxml.jackson.databind.ObjectMapper
import dev.saraki.wofuf.modules.players.services.cache.YesterdayOnlineCache
import dev.saraki.wofuf.modules.players.useCases.getPlayerYesterdayOnline.GetPlayerYesterdayOnlineDto
import dev.saraki.wofuf.shared.services.cache.StringCache
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RedisYesterdayOnlineStringCache(
    private val cache: StringCache,
    private val objectMapper: ObjectMapper
) : YesterdayOnlineCache {

    private val key = "players:yesterday:online"

    override fun get(): GetPlayerYesterdayOnlineDto.Response? {
        val json = cache.get(key) ?: return null
        return objectMapper.readValue(
            json,
            GetPlayerYesterdayOnlineDto.Response::class.java
        )
    }

    override fun put(data: GetPlayerYesterdayOnlineDto.Response) {
        cache.put(
            key,
            objectMapper.writeValueAsString(data),
            Duration.ofHours(24)
        )
    }
}


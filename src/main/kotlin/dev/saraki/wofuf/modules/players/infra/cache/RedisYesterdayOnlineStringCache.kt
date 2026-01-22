package dev.saraki.wofuf.modules.players.infra.cache

import com.fasterxml.jackson.databind.ObjectMapper
import dev.saraki.wofuf.modules.players.services.cache.YesterdayOnlineCache
import dev.saraki.wofuf.modules.players.useCases.getPlayerYesterdayOnlineUseCase.GetPlayerYesterdayOnlineDto
import dev.saraki.wofuf.shared.services.cache.StringCache
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RedisYesterdayOnlineStringCache(
    private val cache: StringCache,
    private val objectMapper: ObjectMapper
) : YesterdayOnlineCache {

    private val key = "players:yesterday:online"

    override fun get(): List<GetPlayerYesterdayOnlineDto>? {
        val json = cache.get(key) ?: return null
        return objectMapper.readValue(
            json,
            objectMapper.typeFactory
                .constructCollectionType(
                    List::class.java,
                    GetPlayerYesterdayOnlineDto::class.java
                )
        )
    }

    override fun put(data: List<GetPlayerYesterdayOnlineDto>) {
        cache.put(
            key,
            objectMapper.writeValueAsString(data),
            Duration.ofHours(2)
        )
    }
}


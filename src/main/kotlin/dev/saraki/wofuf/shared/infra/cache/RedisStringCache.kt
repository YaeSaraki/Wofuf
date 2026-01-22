package dev.saraki.wofuf.shared.infra.cache

import dev.saraki.wofuf.shared.services.cache.StringCache
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RedisStringCache(
    private val redis: StringRedisTemplate
) : StringCache {

    private val ops = redis.opsForValue()

    override fun get(key: String): String? =
        ops.get(key)

    override fun put(key: String, value: String, ttl: Duration) {
        ops.set(key, value, ttl)
    }

    override fun delete(key: String) {
        redis.delete(key)
    }
}

package dev.saraki.wofuf.modules.players.infra.cache

import dev.saraki.wofuf.modules.players.services.cache.PlayerCollectCooldownCache
import dev.saraki.wofuf.modules.players.useCases.collectPlayerData.alc.PlayerResult
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RedisPlayerCollectCooldownCache(
    @Qualifier("PlayerRedisTemplate")
    private val redisTemplate: RedisTemplate<String, Any>
) : PlayerCollectCooldownCache {
    private val opsForValue = redisTemplate.opsForValue()
    private val opsForSet = redisTemplate.opsForSet()
    
    /**
     * 检查玩家是否在冷却中
     */
    override fun isOnCooldown(playerResult: PlayerResult): Boolean {
        val key = "players:collect:cooldown:${playerResult.name}"
        return opsForValue.get(key) != null
    }
    
    /**
     * 设置玩家冷却（默认5分钟）
     */
    override fun setCooldown(playerResult: PlayerResult, minutes: Int) {
        val key = "players:collect:cooldown:${playerResult.name}"
        opsForValue.set(key, "cooldown", Duration.ofMinutes(minutes.toLong()))
    }
    
    /**
     * 清理所有采集相关缓存（用于测试或维护）
     */
    fun clearAll() {
        redisTemplate.delete(redisTemplate.keys("players:collect:*"))
    }
}
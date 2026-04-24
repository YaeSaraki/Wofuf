package dev.saraki.wofuf.modules.forum.infra.events

import dev.saraki.wofuf.modules.forum.domain.events.MemberLoggedOut
import dev.saraki.wofuf.shared.domain.events.IDomainEventHandler
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

/**
 * 成员退出事件处理器
 * 维护本地 JTI 黑名单缓存，同步跨实例的退出状态
 *
 * @author YaeSaraki
 * @date 2026/4/24
 */
@Component
class MemberLogoutEventHandler : IDomainEventHandler<MemberLoggedOut> {

    private val log = KotlinLogging.logger {}

    // 本地 JTI 黑名单缓存：userId -> Set<jti>
    private val localBlacklist = ConcurrentHashMap<String, MutableSet<String>>()

    companion object {
        // 黑名单最大条目数，防止内存无限增长
        private const val MAX_CACHE_SIZE = 10000
        // 每个用户最大 jti 记录数
        private const val MAX_JTI_PER_USER = 100
    }

    override fun handle(event: MemberLoggedOut) {
        val userId = event.getAggregateId().uuid.toString()
        val jti = event.jti

        // 写入本地缓存
        localBlacklist
            .computeIfAbsent(userId) { mutableSetOf() }
            .also {
                it.add(jti)
                // 防止单个用户的 jti 无限增长
                if (it.size > MAX_JTI_PER_USER) {
                    val iterator = it.iterator()
                    iterator.next()
                    iterator.remove()
                }
            }

        // 防止缓存整体无限增长
        if (localBlacklist.size > MAX_CACHE_SIZE) {
            cleanupOldEntries()
        }

        log.info { "Member logout event handled: userId=$userId, jti=$jti, blacklistSize=${localBlacklist.size}" }
    }

    /**
     * 检查指定 JTI 是否已被加入黑名单
     */
    fun isBlacklisted(userId: String, jti: String): Boolean {
        return localBlacklist[userId]?.contains(jti) == true
    }

    /**
     * 清理旧条目（简单策略：清空所有缓存）
     * 生产环境可使用 LRU 或基于 TTL 的策略
     */
    private fun cleanupOldEntries() {
        log.warn { "Blacklist cache exceeded max size, clearing..." }
        localBlacklist.clear()
    }

    override fun getEventType(): Class<out dev.saraki.wofuf.shared.domain.events.IDomainEvent> {
        return MemberLoggedOut::class.java
    }
}
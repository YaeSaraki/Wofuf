package dev.saraki.wofuf.modules.players.useCases.collectPlayerData

import dev.saraki.wofuf.modules.players.domain.valueObjects.PlayerAdvancement
import dev.saraki.wofuf.modules.players.domain.valueObjects.PlayerSkin
import dev.saraki.wofuf.modules.players.domain.valueObjects.PlayerStatistic
import dev.saraki.wofuf.modules.players.services.cache.PlayerCollectCooldownCache
import dev.saraki.wofuf.modules.players.services.yawebapi.PluginApiClient
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Component
class CollectPlayerDataScheduler(
    private val pluginApiClient: PluginApiClient,
    private val collectPlayerDataUseCase: CollectPlayerDataUseCase,
    private val collectedPlayerQueue: MutableSet<UUID> = ConcurrentHashMap.newKeySet(),
    private val playerCollectCooldownCache: PlayerCollectCooldownCache,
    @Value("\${collector.players.pop-delay-ms:60000}")
    private val collectCooldownMs: Int
) {
    private val log = KotlinLogging.logger {}

    // 每 600 秒从队列中弹出一个玩家
    @Scheduled(fixedDelayString = "\${collector.players.pop-delay-ms:600000}")
    fun popCollectedPlayer() {
        collectedPlayerQueue.firstOrNull()?.let {
            collectedPlayerQueue.remove(it)
        }
    }

    /**
     * 每 60 秒采集一次在线玩家数据
     */
    @Scheduled(fixedDelayString = "\${collector.players.delay-ms:60000}")
    fun collectOnlinePlayers() {
        log.info { "开始采集在线玩家数据" }

        val players = try {
            pluginApiClient.fetchOnlinePlayers()
        } catch (e: Exception) {
            log.error(e) { "${"获取在线玩家失败"}: ${e.message}" }
            return
        }

        if (players.isNullOrEmpty()) {
            log.info { "无在线玩家" }
            return
        }

        players.forEach { player ->
            if (collectedPlayerQueue.contains(player.uuid)) {
                return@forEach
            }
            if (playerCollectCooldownCache.isOnCooldown(player)) {
                return@forEach
            }
            collectedPlayerQueue.add(player.uuid)
            playerCollectCooldownCache.setCooldown(player, collectCooldownMs / 60000)

            // 获取统计数据（返回 Map）
            val statisticsResult = pluginApiClient.fetchPlayerStatistics(player.uuid) ?: return@forEach
            val playerStatistics = statisticsResult.statistics.mapValues { (_, props) ->
                PlayerStatistic.create(props)
                    .getOrElse {
                        log.warn { "${"无法创建统计数据: ${props.key}"}: ${it.message}" }
                        PlayerStatistic.defaultProps
                    }
            }

            // 获取进度数据（返回 List）
            val advancementsResult = pluginApiClient.fetchPlayerAdvancements(player.uuid) ?: return@forEach
            val playerAdvancements = advancementsResult.advancements.associateBy(
                keySelector = { it.key },
                valueTransform = { props ->
                    PlayerAdvancement.create(props)
                        .getOrElse {
                            log.warn { "${"无法创建进度数据: ${props.key}"}: ${it.message}" }
                            PlayerAdvancement.defaultProps
                        }
                }
            )

            if (playerStatistics.isEmpty() || playerAdvancements.isEmpty()) {
                log.warn { "玩家 ${player.name} 数据采集失败" }
                return@forEach
            }

            val playerSkin = pluginApiClient.fetchPlayerSkin(player.uuid)
            val type = playerSkin?.type ?: "type"
            val skin = playerSkin?.skin ?: "skin"
            val cape = playerSkin?.cape ?: "cape"

            val result = collectPlayerDataUseCase.execute(
                CollectPlayerDataCommand(
                    uuid = player.uuid.toString(),
                    name = player.name,
                    firstLogin = player.firstLogin,
                    lastLogin = player.lastLogin,
                    totalPlaytimeSeconds = player.totalPlaytimeSeconds,
                    statistics = playerStatistics,
                    advancements = playerAdvancements,
                    playerSkin = PlayerSkin.create(
                        type = type,
                        skin = skin,
                        cape = cape
                    ).getOrThrow()
                )
            )
            if (result.isSuccess) {
                log.info { " 玩家 ${player.name} 数据采集成功" }
            } else {
                log.warn { "${" 玩家 ${player.name} 数据采集失败"}: ${result.exceptionOrNull()?.message}" }
            }
        }
        log.info { " 在线玩家数据采集完成，共 ${players.size} 人" }
    }
}
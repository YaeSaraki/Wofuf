package dev.saraki.wofuf.modules.players.useCases.collectPlayerDataUseCase

import dev.saraki.wofuf.modules.players.domain.PlayerSkin
import dev.saraki.wofuf.modules.players.useCases.collectPlayerDataUseCase.alc.PluginApiClient
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Component
class CollectPlayerDataScheduler(
    private val pluginApiClient: PluginApiClient,
    private val collectPlayerDataUseCase: CollectPlayerDataUseCase,
    private val collectedPlayerQueue: MutableSet<UUID> = ConcurrentHashMap.newKeySet()
) {

    private val log = LoggerFactory.getLogger(javaClass)

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
        log.info("开始采集在线玩家数据")

        val players = try {
            pluginApiClient.fetchOnlinePlayers()
        } catch (e: Exception) {
            log.error("获取在线玩家失败", e)
            return
        }

        if (players == null || players.isEmpty()) {
            log.info("无在线玩家")
            return
        }

        players.forEach { player ->
            if (collectedPlayerQueue.contains(player.uuid)) {
                return@forEach
            }
            collectedPlayerQueue.add(player.uuid)
            // 玩家统计数据
            val playerStatistics = pluginApiClient.fetchPlayerStatistics(player.uuid)
            val playerAdvancements = pluginApiClient.fetchPlayerAdvancements(player.uuid)
            val playerSkin = pluginApiClient.fetchPlayerSkin(player.uuid)

            if (playerStatistics == null || playerAdvancements == null) {
                log.warn("玩家 {} 数据采集失败", player.name)
                return@forEach
            }
            val result = collectPlayerDataUseCase.execute(
                CollectPlayerDataCommand(
                    uuid = player.uuid.toString(),
                    name = player.name,
                    firstLogin = player.firstLogin,
                    lastLogin = player.lastLogin,
                    totalPlaytimeSeconds = player.totalPlaytimeSeconds,
                    statistics = playerStatistics.statistics,
                    advancements = (playerAdvancements.advancements.associateBy { it.key }),
                    playerSkin = PlayerSkin.create(
                        type = playerSkin?.type ?: "",
                        skin = playerSkin?.skin ?: "",
                        cape = playerSkin?.cape ?: ""
                    ).getOrThrow()
                )
            )
            if (result.isSuccess) {
                log.info("✅ 玩家 {} 数据采集成功", player.name)
            } else {
                log.warn("⚠ 玩家 {} 数据采集失败，错误信息：{}", player.name, result.toString())
            }
        }

        log.info("✅ 在线玩家数据采集完成，共 {} 人", players.size)
    }
}
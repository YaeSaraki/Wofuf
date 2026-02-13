package dev.saraki.wofuf.modules.players.useCases.getPlayerYesterdayOnline

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/27 11:27
 *   @description:
 */

@Component
class GetPlayerYesterdayOnlineSheduler(
    private val getPlayerYesterdayOnlineUseCase: GetPlayerYesterdayOnlineUseCase,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    // 每天0点更新昨日在线玩家
    @Scheduled(cron = "0 0 0 * * ?")
    fun updatePlayerYesterdayOnline() {
        try {
            getPlayerYesterdayOnlineUseCase.execute(Unit).getOrThrow()
        } catch (e: Exception) {
            logger.error("更新昨日在线玩家失败", e)
        }
    }
}
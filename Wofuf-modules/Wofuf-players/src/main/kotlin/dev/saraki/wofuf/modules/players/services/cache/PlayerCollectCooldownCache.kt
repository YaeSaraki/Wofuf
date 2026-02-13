package dev.saraki.wofuf.modules.players.services.cache

import dev.saraki.wofuf.modules.players.useCases.collectPlayerData.alc.PlayerResult

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/26 20:17
 *   @description:
 */
interface PlayerCollectCooldownCache {
    fun isOnCooldown(playerResult: PlayerResult): Boolean
    fun setCooldown(playerResult: PlayerResult, minutes: Int = 5)
}
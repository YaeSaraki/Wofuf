package dev.saraki.meovo.modules.yawebapi.domain.reader

import dev.saraki.meovo.modules.yawebapi.domain.PlayerQuery
import dev.saraki.meovo.modules.yawebapi.domain.PlayerResult

interface PlayerReader {

    /**
     * 读取玩家信息
     * - 在线
     * - 离线
     */
    fun read(playerName: String, query: PlayerQuery): PlayerResult?
}
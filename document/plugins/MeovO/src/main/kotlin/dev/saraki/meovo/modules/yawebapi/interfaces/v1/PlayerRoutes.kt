package dev.saraki.meovo.modules.yawebapi.interfaces.v1

import dev.saraki.meovo.modules.yawebapi.domain.PlayerQuery
import dev.saraki.meovo.modules.yawebapi.services.PlayerQueryService
import dev.saraki.meovo.modules.yawebapi.utils.RouteUtil
import fi.iki.elonen.NanoHTTPD

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/19 11:49
 *   @description:
 */
object PlayerRoutes {
    /**
     * 查询所有在线玩家
     */
    fun handleAllOnlinePlayer(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val data = PlayerQueryService.queryAllOnline()
        return RouteUtil.jsonResponseWithAllowOriginsHeader(data)
    }

    /**
     * 查询单个玩家（支持离线）
     */
    fun handleSingle(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val playerName = session.uri.substringAfterLast("/")
        val includeOffline = (session.parameters["offline"]?.firstOrNull()?.toBoolean() ?: true)
        val result = PlayerQueryService.querySingle(
                playerName = playerName,
            PlayerQuery(includeOffline = includeOffline)
        )
        return RouteUtil.jsonResponseWithAllowOriginsHeader(result ?: "")
    }
}
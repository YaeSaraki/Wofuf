package dev.saraki.meovo.modules.yawebapi.interfaces.v1

import dev.saraki.meovo.modules.yawebapi.domain.StatisticQuery
import dev.saraki.meovo.modules.yawebapi.services.StatisticQueryService
import dev.saraki.meovo.modules.yawebapi.utils.RouteUtil
import fi.iki.elonen.NanoHTTPD
import java.util.UUID

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/19 11:17
 *   @description:
 */

object StatisticRoutes {

    fun handleAllOnlinePlayer(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val query = StatisticQuery.Companion.from(session.parameters)
        val data = StatisticQueryService.queryAll(query)
        return RouteUtil.jsonResponseWithAllowOriginsHeader(data)
    }

    fun handleSingle(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val playerUuid = UUID.fromString(session.uri.substringAfterLast("/"))
        val query = StatisticQuery.Companion.from(session.parameters)
        val data = StatisticQueryService.query(
            listOf(playerUuid),
            query)
        return RouteUtil.jsonResponseWithAllowOriginsHeader(data.first())
    }
}
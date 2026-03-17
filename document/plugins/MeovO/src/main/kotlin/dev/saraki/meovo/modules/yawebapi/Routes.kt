package dev.saraki.meovo.modules.yawebapi

import dev.saraki.meovo.modules.yawebapi.config.YaWebApiConfig
import dev.saraki.meovo.modules.yawebapi.interfaces.v1.AdvancementRoutes
import dev.saraki.meovo.modules.yawebapi.interfaces.v1.PlayerRoutes
import dev.saraki.meovo.modules.yawebapi.interfaces.v1.SkinRoutes
import dev.saraki.meovo.modules.yawebapi.interfaces.v1.StatisticRoutes
import fi.iki.elonen.NanoHTTPD

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/12 14:27
 *   @description:
 */
class Routes {
    fun handle(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        if (session.remoteIpAddress !in YaWebApiConfig.getAllowedIpAddresses) {
            return NanoHTTPD.newFixedLengthResponse(
                NanoHTTPD.Response.Status.FORBIDDEN,
                "application/json",
                """{"error":"Forbidden"}"""
            )
        }
        val uri = session.uri
        return when {
            // 成就路由
            uri == "/api/v1/advancements" -> AdvancementRoutes.handleAllOnlinePlayer(session)
            uri.startsWith("/api/v1/advancements/") -> AdvancementRoutes.handleSingleOnlinePlayer(session)

            // 统计信息路由
            uri == "/api/v1/statistics" -> StatisticRoutes.handleAllOnlinePlayer(session)
            uri.startsWith("/api/v1/statistics/") -> StatisticRoutes.handleSingle(session)

            // 玩家路由
            uri == "/api/v1/players" -> PlayerRoutes.handleAllOnlinePlayer(session)
            uri.startsWith("/api/v1/players/") -> PlayerRoutes.handleSingle(session)

            // 皮肤路由
            uri.startsWith("/api/v1/skins/") -> SkinRoutes.handleSingle(session)


            // 其他路由
            else -> NanoHTTPD.newFixedLengthResponse(
                NanoHTTPD.Response.Status.NOT_FOUND,
                "application/json",
                """{"error":"Not Found"}"""
            )
        }
    }
}
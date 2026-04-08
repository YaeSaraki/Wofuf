package dev.saraki.meovo.modules.yawebapi

import dev.saraki.meovo.modules.yawebapi.config.YaWebApiConfig
import dev.saraki.meovo.modules.yawebapi.interfaces.v1.AdvancementRoutes
import dev.saraki.meovo.modules.yawebapi.interfaces.v1.PlayerRoutes
import dev.saraki.meovo.modules.yawebapi.interfaces.v1.ServerRoutes
import dev.saraki.meovo.modules.yawebapi.interfaces.v1.SkinRoutes
import dev.saraki.meovo.modules.yawebapi.interfaces.v1.StatisticRoutes
import fi.iki.elonen.NanoHTTPD
import taboolib.common.platform.function.info

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/12 14:27
 *   @description:
 */
class Routes {
    fun handle(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        info("[Routes] ========== New Request ==========")
        info("[Routes] Remote IP: ${session.remoteIpAddress}")
        info("[Routes] Allowed IPs: ${YaWebApiConfig.getAllowedIpAddresses}")
        info("[Routes] Request URI: ${session.uri}")
        info("[Routes] Request Method: ${session.method}")
        
        if (session.remoteIpAddress !in YaWebApiConfig.getAllowedIpAddresses) {
            info("[Routes] FORBIDDEN - IP not in allowed list")
            return NanoHTTPD.newFixedLengthResponse(
                NanoHTTPD.Response.Status.FORBIDDEN,
                "application/json",
                """{"error":"Forbidden"}"""
            )
        }
        
        val uri = session.uri
        info("[Routes] Routing request for URI: $uri")
        
        return when {
            // 成就路由
            uri == "/api/v1/advancements" -> {
                info("[Routes] Matched: advancements (all)")
                AdvancementRoutes.handleAllOnlinePlayer(session)
            }
            uri.startsWith("/api/v1/advancements/") -> {
                info("[Routes] Matched: advancements (single)")
                AdvancementRoutes.handleSingleOnlinePlayer(session)
            }

            // 统计信息路由
            uri == "/api/v1/statistics" -> {
                info("[Routes] Matched: statistics (all)")
                StatisticRoutes.handleAllOnlinePlayer(session)
            }
            uri.startsWith("/api/v1/statistics/") -> {
                info("[Routes] Matched: statistics (single)")
                StatisticRoutes.handleSingle(session)
            }

            // 玩家路由
            uri == "/api/v1/players" -> {
                info("[Routes] Matched: players (all)")
                PlayerRoutes.handleAllOnlinePlayer(session)
            }
            uri.startsWith("/api/v1/players/") -> {
                info("[Routes] Matched: players (single)")
                PlayerRoutes.handleSingle(session)
            }

            // 皮肤路由
            uri.startsWith("/api/v1/skins/") -> {
                info("[Routes] Matched: skins")
                SkinRoutes.handleSingle(session)
            }

            // 服务器状态路由
            uri == "/api/v1/server/status" -> {
                info("[Routes] Matched: server status")
                ServerRoutes.handleServerStatus(session)
            }

            // 其他路由
            else -> {
                info("[Routes] No match found - returning 404")
                NanoHTTPD.newFixedLengthResponse(
                    NanoHTTPD.Response.Status.NOT_FOUND,
                    "application/json",
                    """{"error":"Not Found"}"""
                )
            }
        }.also {
            info("[Routes] Response status: ${it.status}")
            info("[Routes] ========== Request Complete ==========")
        }
    }
}
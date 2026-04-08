package dev.saraki.meovo.modules.yawebapi.interfaces.v1

import dev.saraki.meovo.modules.yawebapi.services.ServerQueryService
import dev.saraki.meovo.modules.yawebapi.utils.RouteUtil
import fi.iki.elonen.NanoHTTPD
import taboolib.common.platform.function.info

/**
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 * @date 2026/4/8
 * @description: Server status routes for exposing Minecraft server status information
 */
object ServerRoutes {
    /**
     * Handle server status request
     * Returns current online players, TPS, heartbeat status, and update time
     */
    fun handleServerStatus(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        info("[ServerRoutes] ========== Handling Server Status Request ==========")
        info("[ServerRoutes] Remote IP: ${session.remoteIpAddress}")
        
        try {
            // 调用 Service 层获取数据
            info("[ServerRoutes] Calling ServerQueryService.queryStatus()...")
            val status = ServerQueryService.queryStatus()
            info("[ServerRoutes] Status retrieved successfully: $status")

            // 序列化并返回
            info("[ServerRoutes] Returning JSON response")
            return RouteUtil.jsonResponseWithAllowOriginsHeader(status)
        } catch (e: Exception) {
            info("[ServerRoutes] ERROR: ${e.message}")
            e.printStackTrace()
            val errorResponse = mapOf("error" to "Internal Server Error: ${e.message}")
            return RouteUtil.jsonResponseWithAllowOriginsHeader(errorResponse)
        }
    }
}
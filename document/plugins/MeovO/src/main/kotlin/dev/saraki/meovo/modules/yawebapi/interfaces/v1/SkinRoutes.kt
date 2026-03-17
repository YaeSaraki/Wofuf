package dev.saraki.meovo.modules.yawebapi.interfaces.v1

import dev.saraki.meovo.modules.yawebapi.domain.SkinQuery
import dev.saraki.meovo.modules.yawebapi.services.SkinQueryService
import dev.saraki.meovo.modules.yawebapi.utils.RouteUtil
import fi.iki.elonen.NanoHTTPD
import java.util.UUID

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/2 12:06
 *   @description:
 */
object SkinRoutes {
    fun handleSingle(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val playerUuid = session.uri.substringAfterLast("/")
        val includeCape = (session.parameters["cape"]?.firstOrNull()?.toBoolean() ?: true)
        val result = SkinQueryService.querySingle(
            playerUuid = UUID.fromString(playerUuid),
            SkinQuery()
        )
        return RouteUtil.jsonResponseWithAllowOriginsHeader(result ?: "")
    }
}
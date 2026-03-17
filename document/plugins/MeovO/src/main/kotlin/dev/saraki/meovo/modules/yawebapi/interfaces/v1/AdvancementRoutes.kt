package dev.saraki.meovo.modules.yawebapi.interfaces.v1

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/19 11:39
 *   @description:
 */
import dev.saraki.meovo.modules.yawebapi.domain.AdvancementQuery
import dev.saraki.meovo.modules.yawebapi.services.AdvancementQueryService
import dev.saraki.meovo.modules.yawebapi.utils.RouteUtil.jsonResponseWithAllowOriginsHeader
import fi.iki.elonen.NanoHTTPD
import java.util.UUID

object AdvancementRoutes {

    fun handleAllOnlinePlayer(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val query = AdvancementQuery.from(session.parameters)
        val data = AdvancementQueryService.queryAll(query)
        return jsonResponseWithAllowOriginsHeader(data)
    }

    fun handleSingleOnlinePlayer(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val playerUuid = UUID.fromString(session.uri.substringAfter("/api/v1/advancements/"))

        val query = AdvancementQuery.from(session.parameters)
        val data = AdvancementQueryService.query(
            listOf(playerUuid),
            query
        )

        return jsonResponseWithAllowOriginsHeader(data.first())
    }
}
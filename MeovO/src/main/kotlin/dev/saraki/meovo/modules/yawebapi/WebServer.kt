package asia.minecraftserver.yawebapi

import dev.saraki.meovo.modules.yawebapi.Routes
import fi.iki.elonen.NanoHTTPD
import taboolib.common.platform.function.info
import taboolib.common.platform.function.warning

/**
*   @author YaeSaraki 
*   @email ikaraswork@iCloud.com
*   @date 2026/1/12 10:43
*   @description: 
*/
class WebServer(port: Int, private val allowHost: String) : NanoHTTPD(port) {

    private val Routes = Routes()

    override fun serve(session: IHTTPSession): Response {
        val host = session.remoteIpAddress
        val uri = session.uri
        
        info("[WebServer] ========== Incoming Request ==========")
        info("[WebServer] Request URI: $uri")
        info("[WebServer] Remote Host: $host")
        info("[WebServer] Allowed Host: $allowHost")
        
        if (host != allowHost) {
            warning("[WebServer] FORBIDDEN - Host mismatch: $host != $allowHost")
            return newFixedLengthResponse(Response.Status.FORBIDDEN, MIME_PLAINTEXT, "Host not allowed")
        }
        
        info("[WebServer] Host check passed")
        
        return when {
            uri.startsWith("/api/v1/") -> {
                info("[WebServer] Routing to Routes.handle()")
                Routes.handle(session)
            }
            else -> {
                info("[WebServer] URI doesn't start with /api/v1/ - returning 404")
                newFixedLengthResponse(Response.Status.NOT_FOUND, "application/json", """{"error":"Not Found"}""")
            }
        }.also {
            info("[WebServer] Response status: ${it.status}")
            info("[WebServer] ========== Request Complete ==========")
        }
    }
}

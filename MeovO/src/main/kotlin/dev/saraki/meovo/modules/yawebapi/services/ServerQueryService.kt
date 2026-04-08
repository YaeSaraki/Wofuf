package dev.saraki.meovo.modules.yawebapi.services

import dev.saraki.meovo.modules.yawebapi.domain.ServerStatusResult
import dev.saraki.meovo.modules.yawebapi.domain.reader.ServerReader
import dev.saraki.meovo.modules.yawebapi.readers.BukkitServerReader
import taboolib.common.platform.function.info

object ServerQueryService {
    
    // 这里可以直接实例化具体的 Reader实现类，如果你项目中有依赖注入(如 Koin/Dagger)，可以改为注入
    private val serverReader: ServerReader = BukkitServerReader()

    fun queryStatus(): ServerStatusResult {
        info("[ServerQueryService] Querying server status...")
        try {
            val result = serverReader.readStatus()
            info("[ServerQueryService] Status query successful: $result")
            return result
        } catch (e: Exception) {
            info("[ServerQueryService] ERROR querying status: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
}
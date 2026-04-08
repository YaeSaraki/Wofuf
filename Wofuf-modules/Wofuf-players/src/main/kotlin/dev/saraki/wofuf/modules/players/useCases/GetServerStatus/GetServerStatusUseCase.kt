package dev.saraki.wofuf.modules.players.useCases.getServerStatus

import dev.saraki.wofuf.modules.players.domain.ServerStatus
import dev.saraki.wofuf.modules.players.infra.client.MeovoClient
import dev.saraki.wofuf.modules.players.services.cache.ServerStatusCache
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import org.springframework.stereotype.Service

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/4/8
 *   @description: Use case for getting server status with caching
 */
@Service
class GetServerStatusUseCase(
    private val meovoClient: MeovoClient,
    private val cache: ServerStatusCache
) : UseCase<GetServerStatusDto.Request, ServerStatus> {

    override fun execute(request: GetServerStatusDto.Request): Result<ServerStatus> {
        // 1. If force refresh, clear cache first
        if (request.forceRefresh) {
            cache.clear()
        }

        // 2. Try to get from cache
        val cached = cache.get()
        if (cached != null) {
            return Result.success(cached)
        }

        // 3. Cache miss, fetch from Meovo
        val status = meovoClient.getServerStatus()

        if (status == null) {
            return GetServerStatusErrors.ServerStatusNotAvailableError()
        }

        // 4. Update cache
        cache.put(status)

        return Result.success(status)
    }
}

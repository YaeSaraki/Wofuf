package dev.saraki.wofuf.modules.players.useCases.getPlayerYesterdayOnline

import dev.saraki.wofuf.modules.players.infra.repos.PlayerRepo
import dev.saraki.wofuf.modules.players.services.cache.YesterdayOnlineCache
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.ZoneId

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/21 13:44
 *   @description:
 */
@Service
class GetPlayerYesterdayOnlineUseCase(
    private val playerRepository: PlayerRepo,
    private val cache: YesterdayOnlineCache
) : UseCase<Unit, GetPlayerYesterdayOnlineDto.Response> {
    override fun execute(request: Unit): Result<GetPlayerYesterdayOnlineDto.Response> {
        // redis cache
        val cached = cache.get()
        if (cached != null) return Result.success(cached)

        val todayInstant = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val yesterdayInstant =
            LocalDate.now().minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val players = playerRepository.findYesterdayOnline(yesterdayInstant, todayInstant)

        // cache players
        cache.put(GetPlayerYesterdayOnlineDto.Response(players.map { it.playerName.stringValue }))
        return Result.success(GetPlayerYesterdayOnlineDto.Response(players.map { it.playerName.stringValue }))
    }
}
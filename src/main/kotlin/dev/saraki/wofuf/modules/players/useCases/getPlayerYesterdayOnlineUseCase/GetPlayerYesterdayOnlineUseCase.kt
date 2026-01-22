package dev.saraki.wofuf.modules.players.useCases.getPlayerYesterdayOnlineUseCase

import dev.saraki.wofuf.modules.players.domain.repos.PlayerRepository
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
class GetPlayerYesterdayOnlineUseCase(private val playerRepository: PlayerRepository, private val cache: YesterdayOnlineCache) : UseCase<Unit, List<GetPlayerYesterdayOnlineDto>> {
    override fun execute(request: Unit): Result<List<GetPlayerYesterdayOnlineDto>> {
        // redis cache
        val cached = cache.get()
        if (cached != null) return Result.success(cached)

        val todayInstant = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val yesterdayInstant = LocalDate.now().minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val players = playerRepository.findYesterdayOnline(yesterdayInstant, todayInstant)

        // cache players
        cache.put(players.map { GetPlayerYesterdayOnlineDto(it.props.name) })

        return Result.success(players.map { GetPlayerYesterdayOnlineDto(it.props.name) })
    }
}
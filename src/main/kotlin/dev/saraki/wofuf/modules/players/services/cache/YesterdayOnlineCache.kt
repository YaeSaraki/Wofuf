package dev.saraki.wofuf.modules.players.services.cache

import dev.saraki.wofuf.modules.players.useCases.getPlayerYesterdayOnlineUseCase.GetPlayerYesterdayOnlineDto

interface YesterdayOnlineCache {
    fun get(): List<GetPlayerYesterdayOnlineDto>?
    fun put(data: List<GetPlayerYesterdayOnlineDto>)
}
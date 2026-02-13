package dev.saraki.wofuf.modules.players.services.cache

import dev.saraki.wofuf.modules.players.useCases.getPlayerYesterdayOnline.GetPlayerYesterdayOnlineDto

interface YesterdayOnlineCache {
    fun get(): GetPlayerYesterdayOnlineDto.Response?
    fun put(data: GetPlayerYesterdayOnlineDto.Response)
}
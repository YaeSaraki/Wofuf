package dev.saraki.wofuf.modules.players.domain.repos

import dev.saraki.wofuf.modules.players.domain.Player

interface PlayerRepository {

    fun findByUuid(uuid: String): Player?

    fun findByName(name: String): Player?

    fun findRandom(limit: Int = 1): List<Player>

    fun findYesterdayOnline(from: Long, to: Long): List<Player>

    fun countAll(): Long

    fun save(player: Player): Player
}
package dev.saraki.wofuf.modules.players.infra.repos

import dev.saraki.wofuf.modules.players.domain.Player
import dev.saraki.wofuf.modules.players.domain.valueObjects.PlayerId

interface PlayerRepo {
    fun findByPlayerId(playerId: PlayerId): Player?
    fun findByName(name: String): Player?
    fun findRandom(limit: Int = 1): List<Player>
    fun findYesterdayOnline(from: Long, to: Long): List<Player>
    fun countAll(): Long
    fun save(player: Player): Player
    
    /**
     * Search players by name (fuzzy match) or UUID (prefix match)
     * @param query Search query string
     * @param limit Maximum number of results
     * @return List of matching players
     */
    fun searchByQuery(query: String, limit: Int): List<Player>
}
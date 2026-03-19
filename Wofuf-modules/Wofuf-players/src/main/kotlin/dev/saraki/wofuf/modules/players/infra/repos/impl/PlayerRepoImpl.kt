package dev.saraki.wofuf.modules.players.infra.repos.impl

import dev.saraki.wofuf.modules.players.domain.Player
import dev.saraki.wofuf.modules.players.domain.valueObjects.PlayerId
import dev.saraki.wofuf.modules.players.infra.repos.PlayerRepo
import dev.saraki.wofuf.modules.players.infra.repos.jpa.PlayerJpaRepo
import dev.saraki.wofuf.modules.players.infra.repos.jpa.mappers.PlayerEntityMapper
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository

@Repository
class PlayerRepoImpl(
    private val playerJpaRepo: PlayerJpaRepo
) : PlayerRepo {

    override fun findByPlayerId(playerId: PlayerId): Player? {
        val id = playerId.stringValue
        return playerJpaRepo.findById(id)
            .map(PlayerEntityMapper::toDomain)
            .orElse(null)
    }

    override fun findByName(name: String): Player? =
        playerJpaRepo.findByPlayerName(name)
            ?.let(PlayerEntityMapper::toDomain)

    override fun findRandom(limit: Int): List<Player> =
        playerJpaRepo.findRandom(limit)
            .map(PlayerEntityMapper::toDomain)

    override fun findYesterdayOnline(from: Long, to: Long): List<Player> =
        playerJpaRepo.findYesterdayOnline(from, to)
            .map(PlayerEntityMapper::toDomain)

    override fun countAll(): Long =
        playerJpaRepo.count()

    override fun save(player: Player): Player {
        val entity = PlayerEntityMapper.toEntity(player)
        return PlayerEntityMapper.toDomain(playerJpaRepo.save(entity))
    }

    override fun searchByQuery(query: String, limit: Int): List<Player> {
        val pageable = PageRequest.of(0, limit)
        return playerJpaRepo.searchByQuery(query, pageable)
            .map(PlayerEntityMapper::toDomain)
    }
}
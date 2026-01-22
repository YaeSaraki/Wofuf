package dev.saraki.wofuf.modules.players.infra.repos

import dev.saraki.wofuf.modules.players.domain.Player
import dev.saraki.wofuf.modules.players.domain.repos.PlayerRepository
import dev.saraki.wofuf.modules.players.infra.repos.entities.PlayerEntity
import dev.saraki.wofuf.modules.players.infra.repos.mappers.PlayerEntityMapper
import dev.saraki.wofuf.modules.players.infra.repos.mappers.PlayerJsonMapper
import org.springframework.stereotype.Repository

@Repository
class PlayerRepositoryImpl(
    private val jpaRepository: PlayerJpaRepository
) : PlayerRepository {

    override fun findByUuid(uuid: String): Player? =
        jpaRepository.findById(uuid)
            .map(PlayerEntityMapper::toDomain)
            .orElse(null)

    override fun findByName(name: String): Player? =
        jpaRepository.findByName(name)
            ?.let(PlayerEntityMapper::toDomain)

    override fun findRandom(limit: Int): List<Player> =
        jpaRepository.findRandom(limit)
            .map(PlayerEntityMapper::toDomain)

    override fun findYesterdayOnline(from: Long, to: Long): List<Player> =
        jpaRepository.findYesterdayOnline(from, to)
            .map(PlayerEntityMapper::toDomain)

    override fun countAll(): Long =
        jpaRepository.count()

    override fun save(player: Player): Player {
        val entity = PlayerEntity(
            uuid = player.id.value.id,
            name = player.props.name,
            firstLogin = player.props.firstLogin,
            lastLogin = player.props.lastLogin,
            totalPlaytime = player.props.totalPlaytimeSeconds,
            updateTime = player.props.updateTime,
            statisticsJson = PlayerJsonMapper.statisticsToJson(player.statistics),
            advancementsJson = PlayerJsonMapper.advancementsToJson(player.advancements)
        )

        return PlayerEntityMapper.toDomain(jpaRepository.save(entity))
    }
}

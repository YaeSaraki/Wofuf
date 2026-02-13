package dev.saraki.wofuf.modules.players.infra.repos.jpa

import dev.saraki.wofuf.modules.players.infra.repos.jpa.entities.PlayerEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface PlayerJpaRepo : JpaRepository<PlayerEntity, String> {

    fun findByPlayerName(name: String): PlayerEntity?

    @Query(
        """
        SELECT p FROM PlayerEntity p
        ORDER BY RAND() 
        LIMIT :limit
    """
    )
    fun findRandom(limit: Int): List<PlayerEntity>

    @Query(
        """
        SELECT p FROM PlayerEntity p
        WHERE p.lastLogin BETWEEN :from AND :to
    """
    )
    fun findYesterdayOnline(from: Long, to: Long): List<PlayerEntity>
}
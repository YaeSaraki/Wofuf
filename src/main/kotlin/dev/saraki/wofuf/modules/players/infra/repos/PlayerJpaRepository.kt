package dev.saraki.wofuf.modules.players.infra.repos

import dev.saraki.wofuf.modules.players.infra.repos.entities.PlayerEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface PlayerJpaRepository : JpaRepository<PlayerEntity, String> {

    fun findByName(name: String): PlayerEntity?

    @Query(
        value = "SELECT * FROM players ORDER BY RAND() LIMIT :limit",
        nativeQuery = true
    )
    fun findRandom(limit: Int): List<PlayerEntity>

    @Query(
        """
        SELECT * FROM players 
        WHERE last_login BETWEEN :from AND :to
        """,
        nativeQuery = true
    )
    fun findYesterdayOnline(from: Long, to: Long): List<PlayerEntity>
}
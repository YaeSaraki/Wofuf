package dev.saraki.wofuf.modules.players.infra.repos.jpa

import dev.saraki.wofuf.modules.players.infra.repos.jpa.entities.PlayerEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

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

    /**
     * Search players by name (case-insensitive, fuzzy match) or UUID (prefix match)
     * @param query Search query
     * @param pageable Pagination info
     * @return List of matching player entities
     */
    @Query(
        """
        SELECT p FROM PlayerEntity p 
        WHERE LOWER(p.playerName) LIKE LOWER(CONCAT('%', :query, '%')) 
           OR p.playerId LIKE CONCAT(:query, '%') 
        ORDER BY p.playerName ASC
    """
    )
    fun searchByQuery(@Param("query") query: String, pageable: Pageable): List<PlayerEntity>
}
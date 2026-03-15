# Repository Patterns

This document describes the repository patterns used in the Wofuf project for data access abstraction.

## Repository Interface Pattern

### Basic Repository Interface
```kotlin
package dev.saraki.wofuf.modules.example.infra.repos

import dev.saraki.wofuf.modules.example.domain.Entity
import dev.saraki.wofuf.modules.example.domain.valueObjects.EntityId

interface EntityRepo {
    fun findById(id: EntityId): Entity?
    fun findByCriteria(criteria: Criteria): List<Entity>
    fun save(entity: Entity): Entity
    fun delete(id: EntityId)
    fun exists(id: EntityId): Boolean
}
```

### Common Repository Methods
```kotlin
interface PlayerRepo {
    // Basic CRUD
    fun findByPlayerId(playerId: PlayerId): Player?
    fun findByName(name: String): Player?
    fun save(player: Player): Player
    fun delete(playerId: PlayerId)

    // Specialized queries
    fun findRandom(limit: Int = 1): List<Player>
    fun findYesterdayOnline(from: Long, to: Long): List<Player>
    fun countAll(): Long

    // Existence checks
    fun exists(playerId: PlayerId): Boolean
}
```

### Forum Repository Interface
```kotlin
interface PostRepo {
    fun findPostByPostId(postId: PostId): Post?
    fun findPostBySlug(postSlug: PostSlug): Post?
    fun findRecentPosts(offset: Int? = null): List<Post>
    fun findPopularPosts(offset: Int? = null): List<Post>
    fun findNumberOfCommentsByPostId(postId: PostId): Int?
    fun exists(postId: PostId): Boolean
    fun save(post: Post): Post
    fun delete(postId: PostId)
}
```

## Repository Implementation Pattern

### Basic Implementation Structure
```kotlin
package dev.saraki.wofuf.modules.example.infra.repos.impl

import dev.saraki.wofuf.modules.example.domain.Entity
import dev.saraki.wofuf.modules.example.domain.valueObjects.EntityId
import dev.saraki.wofuf.modules.example.infra.repos.EntityRepo
import dev.saraki.wofuf.modules.example.infra.repos.jpa.EntityJpaRepo
import dev.saraki.wofuf.modules.example.infra.repos.jpa.mappers.EntityMapper
import org.springframework.stereotype.Repository

@Repository
class EntityRepoImpl(
    private val entityJpaRepo: EntityJpaRepo
) : EntityRepo {

    override fun findById(id: EntityId): Entity? {
        return entityJpaRepo.findById(id.stringValue)
            .map(EntityMapper::toDomain)
            .orElse(null)
    }

    override fun findByCriteria(criteria: Criteria): List<Entity> {
        return entityJpaRepo.findByCriteria(criteria)
            .map(EntityMapper::toDomain)
    }

    override fun save(entity: Entity): Entity {
        val jpaEntity = EntityMapper.toEntity(entity)
        val savedEntity = entityJpaRepo.save(jpaEntity)
        return EntityMapper.toDomain(savedEntity)
    }

    override fun delete(id: EntityId) {
        entityJpaRepo.deleteById(id.stringValue)
    }

    override fun exists(id: EntityId): Boolean {
        return entityJpaRepo.existsById(id.stringValue)
    }
}
```

### Player Repository Implementation
```kotlin
package dev.saraki.wofuf.modules.players.infra.repos.impl

import dev.saraki.wofuf.modules.players.domain.Player
import dev.saraki.wofuf.modules.players.domain.valueObjects.PlayerId
import dev.saraki.wofuf.modules.players.infra.repos.PlayerRepo
import dev.saraki.wofuf.modules.players.infra.repos.jpa.PlayerJpaRepo
import dev.saraki.wofuf.modules.players.infra.repos.jpa.mappers.PlayerEntityMapper
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

    override fun exists(playerId: PlayerId): Boolean =
        playerJpaRepo.existsById(playerId.stringValue)

    override fun delete(playerId: PlayerId) {
        playerJpaRepo.deleteById(playerId.stringValue)
    }
}
```

## JPA Repository Patterns

### Basic JPA Repository
```kotlin
package dev.saraki.wofuf.modules.example.infra.repos.jpa

import dev.saraki.wofuf.modules.example.infra.repos.jpa.entities.EntityEntity
import org.springframework.data.jpa.repository.JpaRepository

interface EntityJpaRepo : JpaRepository<EntityEntity, String> {
    // Custom query methods will be added here
}
```

### Advanced JPA Repository
```kotlin
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
```

### Complex Query Examples
```kotlin
interface PostJpaRepo : JpaRepository<PostEntity, String> {

    @Query("""
        SELECT p FROM PostEntity p
        LEFT JOIN FETCH p.comments
        WHERE p.slug = :slug
    """)
    fun findBySlugWithComments(@Param("slug") slug: String): PostEntity?

    @Query("""
        SELECT p FROM PostEntity p
        WHERE p.dateTimePosted >= :since
        ORDER BY p.points DESC, p.dateTimePosted DESC
    """)
    fun findPopularPostsSince(
        @Param("since") since: LocalDateTime,
        pageable: Pageable
    ): Page<PostEntity>

    @Query("""
        SELECT COUNT(c) FROM CommentEntity c
        WHERE c.postId = :postId
    """)
    fun countCommentsByPostId(@Param("postId") postId: String): Long
}
```

## Entity Mapping Patterns

### Basic Entity Mapper
```kotlin
package dev.saraki.wofuf.modules.example.infra.repos.jpa.mappers

import dev.saraki.wofuf.modules.example.domain.Entity
import dev.saraki.wofuf.modules.example.infra.repos.jpa.entities.EntityEntity

object EntityMapper {

    fun toDomain(entity: EntityEntity): Entity {
        return Entity.create(
            props = EntityProps(
                name = EntityName.create(entity.name).getOrThrow(),
                description = entity.description?.let { EntityDescription.create(it).getOrThrow() }
            ),
            id = UniqueEntityId(entity.id)
        ).getOrThrow()
    }

    fun toEntity(domain: Entity): EntityEntity {
        return EntityEntity(
            id = domain.entityId.stringValue,
            name = domain.name.value,
            description = domain.description?.value
        )
    }
}
```

### Complex Entity Mapping
```kotlin
object PlayerEntityMapper {

    fun toDomain(entity: PlayerEntity): Player {
        return Player.create(
            props = PlayerProps(
                playerName = PlayerName.create(entity.playerName).getOrThrow(),
                firstLogin = entity.firstLogin,
                lastLogin = entity.lastLogin,
                totalPlaytimeSeconds = entity.totalPlaytimeSeconds,
                updateTime = entity.updateTime,
                statistics = entity.statistics.mapValues { (key, value) ->
                    PlayerStatistic.create(key, value).getOrThrow()
                },
                advancements = entity.advancements.mapValues { (key, value) ->
                    PlayerAdvancement.create(key, value).getOrThrow()
                },
                playerSkin = PlayerSkin.create(entity.skinData).getOrThrow()
            ),
            id = UniqueEntityId(entity.id)
        ).getOrThrow()
    }

    fun toEntity(player: Player): PlayerEntity {
        return PlayerEntity(
            id = player.playerId.stringValue,
            playerName = player.playerName.value,
            firstLogin = player.firstLogin,
            lastLogin = player.lastLogin,
            totalPlaytimeSeconds = player.totalPlaytimeSeconds,
            updateTime = player.updateTime,
            statistics = player.statistics.mapValues { it.value.value },
            advancements = player.advancements.mapValues { it.value.value },
            skinData = player.playerSkin.textureValue
        )
    }
}
```

## Repository Testing Patterns

### Unit Test with Mocks
```kotlin
@SpringBootTest
class PlayerRepoTest {

    @Autowired
    lateinit var playerRepo: PlayerRepo

    @MockBean
    lateinit var playerJpaRepo: PlayerJpaRepo

    @Test
    fun `should find player by id`() {
        // Given
        val playerId = PlayerId.create(UniqueEntityId()).getOrThrow()
        val playerEntity = createPlayerEntity()
        given(playerJpaRepo.findById(playerId.stringValue)).willReturn(Optional.of(playerEntity))

        // When
        val result = playerRepo.findByPlayerId(playerId)

        // Then
        assertNotNull(result)
        assertEquals(playerEntity.playerName, result?.playerName?.value)
    }

    @Test
    fun `should return null when player not found`() {
        // Given
        val playerId = PlayerId.create(UniqueEntityId()).getOrThrow()
        given(playerJpaRepo.findById(playerId.stringValue)).willReturn(Optional.empty())

        // When
        val result = playerRepo.findByPlayerId(playerId)

        // Then
        assertNull(result)
    }
}
```

### Integration Test
```kotlin
@SpringBootTest
@Sql("/test-data.sql")
class PlayerRepoIntegrationTest {

    @Autowired
    lateinit var playerRepo: PlayerRepo

    @Test
    fun `should save and retrieve player`() {
        // Given
        val player = createTestPlayer()

        // When
        val savedPlayer = playerRepo.save(player)
        val retrievedPlayer = playerRepo.findByPlayerId(savedPlayer.playerId)

        // Then
        assertNotNull(retrievedPlayer)
        assertEquals(savedPlayer.playerName, retrievedPlayer?.playerName)
    }

    @Test
    fun `should find random players`() {
        // When
        val randomPlayers = playerRepo.findRandom(5)

        // Then
        assertTrue(randomPlayers.size <= 5)
        randomPlayers.forEach { player ->
            assertNotNull(player.playerName)
            assertTrue(player.firstLogin > 0)
        }
    }
}
```

## Repository Configuration

### JPA Configuration
```kotlin
@Configuration
@EnableJpaRepositories(
    basePackages = ["dev.saraki.wofuf.modules.players.infra.repos.jpa"]
)
@EnableTransactionManagement
class JpaConfig {

    @Bean
    fun entityManagerFactory(
        dataSource: DataSource,
        jpaProperties: JpaProperties
    ): LocalContainerEntityManagerFactoryBean {
        return LocalContainerEntityManagerFactoryBean().apply {
            this.dataSource = dataSource
            setPackagesToScan("dev.saraki.wofuf.modules.players.infra.repos.jpa.entities")
            jpaVendorAdapter = HibernateJpaVendorAdapter()
            setJpaProperties(jpaProperties.properties)
        }
    }

    @Bean
    fun transactionManager(entityManagerFactory: EntityManagerFactory): JpaTransactionManager {
        return JpaTransactionManager(entityManagerFactory)
    }
}
```

## Performance Considerations

### N+1 Query Prevention
```kotlin
// Bad - causes N+1 queries
@Repository
class BadPostRepoImpl(private val postJpaRepo: PostJpaRepo) : PostRepo {
    override fun findPostsWithComments(): List<Post> {
        val posts = postJpaRepo.findAll()
        return posts.map { postEntity ->
            val comments = commentJpaRepo.findByPostId(postEntity.id) // N queries
            PostMapper.toDomain(postEntity, comments)
        }
    }
}

// Good - uses JOIN FETCH
@Repository
class GoodPostRepoImpl(private val postJpaRepo: PostJpaRepo) : PostRepo {
    override fun findPostsWithComments(): List<Post> {
        return postJpaRepo.findAllWithComments() // Single query with JOIN
            .map { PostMapper.toDomain(it) }
    }
}
```

### Query Method Naming
```kotlin
interface PostJpaRepo : JpaRepository<PostEntity, String> {
    // Good - follows Spring Data naming conventions
    fun findByAuthorIdAndDateTimePostedGreaterThan(
        authorId: String,
        dateTime: LocalDateTime
    ): List<PostEntity>

    // Good - uses @Query for complex queries
    @Query("SELECT p FROM PostEntity p WHERE p.points > :minPoints")
    fun findPopularPosts(@Param("minPoints") minPoints: Int): List<PostEntity>
}
```

### Caching Strategies
```kotlin
@Repository
@CacheConfig(cacheNames = ["players"])
class PlayerRepoImpl(private val playerJpaRepo: PlayerJpaRepo) : PlayerRepo {

    @Cacheable(key = "#playerId.stringValue")
    override fun findByPlayerId(playerId: PlayerId): Player? {
        return playerJpaRepo.findById(playerId.stringValue)
            .map(PlayerEntityMapper::toDomain)
            .orElse(null)
    }

    @CacheEvict(key = "#player.playerId.stringValue")
    override fun save(player: Player): Player {
        val entity = PlayerEntityMapper.toEntity(player)
        val savedEntity = playerJpaRepo.save(entity)
        return PlayerEntityMapper.toDomain(savedEntity)
    }
}
```

## Error Handling in Repositories

### Repository-Level Validation
```kotlin
@Repository
class PostRepoImpl(private val postJpaRepo: PostJpaRepo) : PostRepo {

    override fun save(post: Post): Post {
        try {
            // Validate business rules before saving
            validatePost(post)

            val entity = PostEntityMapper.toEntity(post)
            val savedEntity = postJpaRepo.save(entity)
            return PostEntityMapper.toDomain(savedEntity)
        } catch (e: DataIntegrityViolationException) {
            when {
                e.message?.contains("slug_UNIQUE") == true ->
                    throw DuplicateSlugException(post.slug.value)
                e.message?.contains("author_id") == true ->
                    throw InvalidAuthorException(post.authorId.stringValue)
                else -> throw RepositoryException("Failed to save post", e)
            }
        }
    }

    private fun validatePost(post: Post) {
        if (post.title.value.length < 5) {
            throw ValidationException("Post title too short")
        }
    }
}
```

Repository patterns provide:
- **Data Access Abstraction**: Clean separation between domain and data layers
- **Testability**: Easy to mock and test data access logic
- **Performance**: Optimized queries and caching strategies
- **Maintainability**: Centralized data access logic
- **Error Handling**: Consistent error handling and validation</content>
<parameter name="filePath">/Users/saraki/Documents/project/Wofuf/document/agents/Infrastructure/Repositories.md

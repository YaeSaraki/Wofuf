# Mapping Patterns

This document describes the mapping patterns used to convert between domain objects and infrastructure entities.

## Entity-Domain Mapping Pattern

### Basic Mapper Structure
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

## Complex Domain Mapping

### Player Entity Mapping
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

### Post Entity Mapping with Relationships
```kotlin
object PostEntityMapper {

    fun toDomain(entity: PostEntity): Post {
        return Post.create(
            props = PostProps(
                memberId = MemberId.create(UniqueEntityId(entity.authorId)).getOrThrow(),
                slug = PostSlug.createFromExisting(entity.slug).getOrThrow(),
                title = PostTitle.create(entity.title).getOrThrow(),
                type = PostType.valueOf(entity.type),
                text = entity.text?.let { PostText.create(it).getOrThrow() },
                link = entity.link?.let { PostLink.create(it).getOrThrow() },
                comments = Comments.create(
                    entity.comments.map { CommentEntityMapper.toDomain(it) }
                ),
                votes = PostVotes.create(entity.votes),
                totalNumComments = entity.totalNumComments,
                points = entity.points,
                dateTimePosted = entity.dateTimePosted
            ),
            id = UniqueEntityId(entity.id)
        ).getOrThrow()
    }

    fun toEntity(post: Post): PostEntity {
        return PostEntity(
            id = post.postId.stringValue,
            authorId = post.memberId.stringValue,
            slug = post.slug.value,
            title = post.title.value,
            type = post.type.name,
            text = post.text?.value,
            link = post.link?.value,
            comments = post.comments.map { CommentEntityMapper.toEntity(it) },
            votes = post.votes.toMap(),
            totalNumComments = post.totalNumComments,
            points = post.points,
            dateTimePosted = post.dateTimePosted
        )
    }
}
```

## DTO Mapping Patterns

### Use Case DTO Mapping
```kotlin
abstract class GetPlayerDtoMap {
    companion object {
        fun from(player: Player): GetPlayerDto.Response =
            GetPlayerDto.Response(
                id = player.playerId.stringValue,
                name = player.playerName.value,
                firstLogin = player.firstLogin,
                lastLogin = player.lastLogin,
                totalPlaytimeSeconds = player.totalPlaytimeSeconds,
                updateTime = player.updateTime,
            )
    }
}
```

### Complex DTO Mapping
```kotlin
abstract class GetPostDtoMap {
    companion object {
        fun from(post: Post): GetPostDto.Response {
            return GetPostDto.Response(
                id = post.postId.stringValue,
                slug = post.slug.value,
                title = post.title.value,
                type = post.type.name,
                text = post.text?.value,
                link = post.link?.value,
                author = MemberSummaryDto(
                    id = post.memberId.stringValue,
                    name = "Author Name" // Would be fetched separately
                ),
                comments = post.comments.map { CommentSummaryDto(
                    id = it.commentId.stringValue,
                    author = it.memberId.stringValue,
                    content = it.text.value,
                    createdAt = it.createdAt
                )},
                votes = post.points,
                createdAt = post.dateTimePosted.toEpochMilli(),
                updatedAt = post.dateTimePosted.toEpochMilli()
            )
        }
    }
}
```

## Collection Mapping Patterns

### List Mapping
```kotlin
fun mapToDomainList(entities: List<EntityEntity>): List<Entity> {
    return entities.map { toDomain(it) }
}

fun mapToEntityList(domains: List<Entity>): List<EntityEntity> {
    return domains.map { toEntity(it) }
}
```

### Page Mapping
```kotlin
fun mapToDomainPage(entityPage: Page<EntityEntity>): Page<Entity> {
    val domainContent = entityPage.content.map { toDomain(it) }
    return PageImpl(
        domainContent,
        entityPage.pageable,
        entityPage.totalElements
    )
}

fun mapToDtoPage(domainPage: Page<Entity>): Page<EntityDto> {
    val dtoContent = domainPage.content.map { EntityDto.from(it) }
    return PageImpl(
        dtoContent,
        domainPage.pageable,
        domainPage.totalElements
    )
}
```

## Nested Object Mapping

### Complex Object Mapping
```kotlin
object UserProfileMapper {
    fun toDomain(entity: UserProfileEntity): UserProfile {
        return UserProfile.create(
            props = UserProfileProps(
                userId = UserId.create(UniqueEntityId(entity.userId)).getOrThrow(),
                displayName = DisplayName.create(entity.displayName).getOrThrow(),
                bio = entity.bio?.let { Bio.create(it).getOrThrow() },
                avatar = entity.avatar?.let { AvatarUrl.create(it).getOrThrow() },
                preferences = UserPreferences(
                    theme = Theme.valueOf(entity.theme),
                    language = Language.valueOf(entity.language),
                    notifications = NotificationSettings(
                        email = entity.emailNotifications,
                        push = entity.pushNotifications
                    )
                ),
                socialLinks = SocialLinks(
                    twitter = entity.twitter?.let { TwitterHandle.create(it).getOrThrow() },
                    github = entity.github?.let { GitHubUsername.create(it).getOrThrow() },
                    website = entity.website?.let { WebsiteUrl.create(it).getOrThrow() }
                )
            ),
            id = UniqueEntityId(entity.id)
        ).getOrThrow()
    }

    fun toEntity(domain: UserProfile): UserProfileEntity {
        return UserProfileEntity(
            id = domain.id.uuid.toString(),
            userId = domain.userId.stringValue,
            displayName = domain.displayName.value,
            bio = domain.bio?.value,
            avatar = domain.avatar?.value,
            theme = domain.preferences.theme.name,
            language = domain.preferences.language.name,
            emailNotifications = domain.preferences.notifications.email,
            pushNotifications = domain.preferences.notifications.push,
            twitter = domain.socialLinks.twitter?.value,
            github = domain.socialLinks.github?.value,
            website = domain.socialLinks.website?.value
        )
    }
}
```

## JSON Mapping Patterns

### Gson-based Mapping
```kotlin
object PlayerSkinMapper {
    private val gson = Gson()

    fun toDomain(jsonString: String): PlayerSkin {
        try {
            val skinData = gson.fromJson(jsonString, SkinData::class.java)
            return PlayerSkin.create(skinData).getOrThrow()
        } catch (e: Exception) {
            throw MappingException("Failed to parse player skin data", e)
        }
    }

    fun toJson(domain: PlayerSkin): String {
        val skinData = SkinData(
            textureValue = domain.textureValue,
            signature = domain.textureSignature
        )
        return gson.toJson(skinData)
    }

    private data class SkinData(
        val textureValue: String,
        val signature: String
    )
}
```

### Complex JSON Mapping
```kotlin
object PlayerStatisticsMapper {
    private val gson = Gson()

    fun toDomain(jsonString: String): Map<String, PlayerStatistic> {
        if (jsonString.isBlank()) return emptyMap()

        try {
            val rawStats: Map<String, Int> = gson.fromJson(jsonString, object : TypeToken<Map<String, Int>>() {}.type)
            return rawStats.mapValues { (key, value) ->
                PlayerStatistic.create(key, value).getOrThrow()
            }
        } catch (e: Exception) {
            throw MappingException("Failed to parse player statistics", e)
        }
    }

    fun toJson(domain: Map<String, PlayerStatistic>): String {
        val rawStats = domain.mapValues { it.value.value }
        return gson.toJson(rawStats)
    }
}
```

## Mapping Error Handling

### Validation in Mapping
```kotlin
object PostMapper {
    fun toDomain(entity: PostEntity): Post {
        try {
            return Post.create(
                props = PostProps(
                    memberId = MemberId.create(UniqueEntityId(entity.authorId))
                        .getOrElse { throw MappingException("Invalid member ID: ${entity.authorId}") },
                    slug = PostSlug.createFromExisting(entity.slug)
                        .getOrElse { throw MappingException("Invalid slug: ${entity.slug}") },
                    title = PostTitle.create(entity.title)
                        .getOrElse { throw MappingException("Invalid title: ${entity.title}") },
                    // ... other validations
                ),
                id = UniqueEntityId(entity.id)
            ).getOrThrow()
        } catch (e: Exception) {
            throw MappingException("Failed to map PostEntity to Post domain", e)
        }
    }
}
```

### Custom Mapping Exceptions
```kotlin
class MappingException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

class EntityNotFoundException(
    entityType: String,
    id: String
) : MappingException("Entity $entityType with id $id not found")

class ValidationMappingException(
    field: String,
    value: Any?,
    reason: String
) : MappingException("Field '$field' with value '$value' failed validation: $reason")
```

## Testing Mappers

### Unit Test for Mappers
```kotlin
class PlayerEntityMapperTest {

    @Test
    fun `should map entity to domain correctly`() {
        // Given
        val entity = PlayerEntity(
            id = "player-123",
            playerName = "testPlayer",
            firstLogin = 1000000L,
            lastLogin = 2000000L,
            totalPlaytimeSeconds = 3600L,
            updateTime = 2000000L,
            statistics = mapOf("blocksMined" to 100),
            advancements = mapOf("story/mine_stone" to 1),
            skinData = "{}"
        )

        // When
        val domain = PlayerEntityMapper.toDomain(entity)

        // Then
        assertEquals("player-123", domain.playerId.stringValue)
        assertEquals("testPlayer", domain.playerName.value)
        assertEquals(1000000L, domain.firstLogin)
        assertEquals(1, domain.advancements.size)
    }

    @Test
    fun `should map domain to entity correctly`() {
        // Given
        val domain = createTestPlayer()

        // When
        val entity = PlayerEntityMapper.toEntity(domain)

        // Then
        assertEquals(domain.playerId.stringValue, entity.id)
        assertEquals(domain.playerName.value, entity.playerName)
        assertEquals(domain.statistics.size, entity.statistics.size)
    }
}
```

### Integration Test for Mappers
```kotlin
@SpringBootTest
class MappingIntegrationTest {

    @Autowired
    lateinit var playerRepo: PlayerRepo

    @Test
    fun `should maintain data integrity through save and retrieve cycle`() {
        // Given
        val originalPlayer = createComplexPlayer()

        // When
        val savedPlayer = playerRepo.save(originalPlayer)
        val retrievedPlayer = playerRepo.findByPlayerId(savedPlayer.playerId)

        // Then
        assertNotNull(retrievedPlayer)
        assertEquals(originalPlayer.playerName, retrievedPlayer?.playerName)
        assertEquals(originalPlayer.statistics, retrievedPlayer?.statistics)
        assertEquals(originalPlayer.advancements, retrievedPlayer?.advancements)
    }
}
```

## Performance Considerations

### Lazy Loading in Mappers
```kotlin
object PostMapper {
    // Avoid loading comments unless needed
    fun toDomainSummary(entity: PostEntity): PostSummary {
        return PostSummary(
            id = entity.id,
            title = entity.title,
            authorId = entity.authorId,
            commentCount = entity.totalNumComments ?: 0,
            points = entity.points
        )
    }

    // Load full post with comments when needed
    fun toDomainFull(entity: PostEntity): Post {
        val comments = entity.comments.map { CommentMapper.toDomain(it) }
        // ... full mapping logic
    }
}
```

### Batch Mapping
```kotlin
object BatchMapper {
    fun toDomainBatch(entities: List<EntityEntity>): List<Entity> {
        // Pre-load related data if needed
        val relatedData = loadRelatedData(entities.map { it.id })

        return entities.map { entity ->
            toDomain(entity, relatedData[entity.id])
        }
    }

    private fun loadRelatedData(ids: List<String>): Map<String, RelatedData> {
        // Batch load related data to avoid N+1 queries
        return relatedDataRepo.findByEntityIds(ids)
            .associateBy { it.entityId }
    }
}
```

Mapping patterns ensure:
- **Data Integrity**: Proper conversion between layers
- **Type Safety**: Strong typing throughout the application
- **Performance**: Efficient data transformation
- **Maintainability**: Centralized mapping logic
- **Testability**: Easy to test mapping transformations</content>
<parameter name="filePath">/Users/saraki/Documents/project/Wofuf/document/agents/Infrastructure/Mappers.md

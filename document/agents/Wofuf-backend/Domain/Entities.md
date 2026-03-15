# Domain Entities

This document describes the patterns and conventions for implementing domain entities in the Wofuf project.

## Aggregate Root Pattern

### Basic Structure
All domain entities inherit from `AggregateRoot<T>` and follow this pattern:

```kotlin
data class EntityProps(
    val field1: ValueObject1,
    val field2: ValueObject2,
    // ... other properties
)

class Entity private constructor(
    props: EntityProps,
    id: UniqueEntityId?
) : AggregateRoot<EntityProps>(props, id) {

    // Computed properties
    val entityId: EntityId
        get() = EntityId.create(_id).getOrThrow()

    val field1: ValueObject1
        get() = props.field1

    // Business methods
    fun updateField1(newValue: ValueObject1): Result<Entity> {
        val newProps = props.copy(field1 = newValue)
        return create(newProps, id)
    }

    companion object {
        fun create(props: EntityProps, id: UniqueEntityId? = null): Result<Entity> {
            // Validation logic
            val guardResult = Guard.againstNullOrUndefinedBulk(listOf(
                Guard.GuardArgument(props.field1, "field1 cannot be null")
            ))

            if (guardResult.isFailure) {
                return Result.failure(guardResult.exceptionOrThrow())
            }

            return Result.success(Entity(props, id))
        }
    }
}
```

### Key Characteristics

1. **Private Constructor**: Entities can only be created through factory methods
2. **Props Data Class**: Immutable properties container
3. **Computed Properties**: Lazy getters for domain-specific IDs and values
4. **Business Methods**: Domain behavior encapsulated within the entity
5. **Factory Method**: `create()` companion function with validation

### Example: Player Entity

```kotlin
data class PlayerProps(
    val playerName: PlayerName,
    val firstLogin: Long,
    val lastLogin: Long,
    val totalPlaytimeSeconds: Long,
    val updateTime: Long,
    val statistics: Map<String, PlayerStatistic>,
    val advancements: Map<String, PlayerAdvancement>,
    val playerSkin: PlayerSkin
)

class Player private constructor(
    props: PlayerProps,
    id: UniqueEntityId?,
) : AggregateRoot<PlayerProps>(props, id) {

    val playerId: PlayerId
        get() = PlayerId.create(_id).getOrThrow()

    val playerName: PlayerName
        get() = props.playerName

    val firstLogin: Long
        get() = props.firstLogin

    val lastLogin: Long
        get() = props.lastLogin

    val totalPlaytimeSeconds: Long
        get() = props.totalPlaytimeSeconds

    val updateTime: Long
        get() = props.updateTime

    val advancements: Map<String, PlayerAdvancement>
        get() = props.advancements

    val statistics: Map<String, PlayerStatistic>
        get() = props.statistics

    val playerSkin: PlayerSkin
        get() = props.playerSkin

    fun updateProps(props: PlayerProps): Result<Player> {
        return create(props, id)
    }

    companion object {
        fun create(
            props: PlayerProps,
            id: UniqueEntityId?
        ): Result<Player> {
            val guardResult = Guard.againstNullOrUndefinedBulk(
                listOf(
                    Guard.GuardArgument(props.playerName, "Player name cannot be null or blank")
                )
            )
            if (guardResult.isFailure) {
                return Result.failure(guardResult.exceptionOrThrow())
            }

            val defaultProps = props.copy(
                firstLogin = props.firstLogin,
                lastLogin = props.lastLogin,
                totalPlaytimeSeconds = props.totalPlaytimeSeconds,
                updateTime = props.updateTime,
                statistics = props.statistics,
                advancements = props.advancements,
                playerSkin = props.playerSkin,
            )
            val player = Player(defaultProps, id)
            return Result.success(player)
        }
    }
}
```

## Entity Relationships

### One-to-Many: Post and Comments

```kotlin
data class PostProps(
    val memberId: MemberId,
    val slug: PostSlug,
    val title: PostTitle,
    val type: PostType,
    val text: PostText?,
    val link: PostLink?,
    val comments: Comments,  // Collection of comments
    val votes: PostVotes,
    val totalNumComments: Int?,
    val points: Int,
    val dateTimePosted: LocalDateTime
)

class Post private constructor(
    props: PostProps,
    id: UniqueEntityId?
) : AggregateRoot<PostProps>(props, id) {

    // ... other properties

    val comments: Comments
        get() = props.comments

    // Business method to add comment
    fun addComment(memberId: MemberId, postId: PostId, text: CommentText, parentCommentId: CommentId?): Result<Post> {
        val commentProps = CommentProps(
            postId = postId,
            text = text,
            memberId = memberId,
            parentCommentId = parentCommentId,
            points = 0,
            votes = CommentVotes.create(),
        )
        val comment = Comment.create(commentProps).getOrThrow()
        this.comments.add(comment)

        val newProps = props.copy(
            comments = this.comments,
            totalNumComments = (props.totalNumComments ?: 0) + 1
        )
        return Result.success(Post(newProps, id))
    }

    // ... companion object
}
```

### Comments Collection

```kotlin
class Comments private constructor(
    private val comments: MutableList<Comment> = mutableListOf()
) : Iterable<Comment> by comments {

    val size: Int
        get() = comments.size

    fun add(comment: Comment) {
        comments.add(comment)
    }

    fun remove(comment: Comment) {
        comments.remove(comment)
    }

    fun findById(commentId: CommentId): Comment? {
        return comments.find { it.commentId == commentId }
    }

    companion object {
        fun create(comments: List<Comment> = emptyList()): Comments {
            return Comments(comments.toMutableList())
        }
    }
}
```

## Domain Events

### Event Definition
```kotlin
package dev.saraki.wofuf.modules.example.domain.events

import dev.saraki.wofuf.shared.domain.UniqueEntityId
import dev.saraki.wofuf.shared.domain.events.IDomainEvent
import java.time.LocalDateTime

data class ExampleEvent(
    private val aggregateId: UniqueEntityId,
    val data: String,
    override val dataTimeOccurred: LocalDateTime = LocalDateTime.now()
) : IDomainEvent {
    override fun getAggregateId(): UniqueEntityId {
        return aggregateId
    }
}
```

### Publishing Events
```kotlin
class Entity private constructor(
    props: EntityProps,
    id: UniqueEntityId?
) : AggregateRoot<EntityProps>(props, id) {

    fun performAction(): Result<Entity> {
        // Business logic
        // ...

        // Publish domain event
        addDomainEvent(ExampleEvent(
            aggregateId = this.id,
            data = "action performed"
        ))

        return Result.success(this)
    }
}
```

## Business Rules and Validation

### Guard Patterns
```kotlin
companion object {
    fun create(props: EntityProps, id: UniqueEntityId? = null): Result<Entity> {
        // Null/undefined checks
        val guardResult = Guard.againstNullOrUndefinedBulk(listOf(
            Guard.GuardArgument(props.requiredField, "requiredField cannot be null")
        ))

        if (guardResult.isFailure) {
            return Result.failure(guardResult.exceptionOrThrow())
        }

        // Business rule validation
        if (props.amount < 0) {
            return Result.failure(UseCaseError("INVALID_AMOUNT", "Amount cannot be negative"))
        }

        // Domain-specific validation
        if (props.name.value.length < 3) {
            return Result.failure(UseCaseError("NAME_TOO_SHORT", "Name must be at least 3 characters"))
        }

        return Result.success(Entity(props, id))
    }
}
```

### Business Methods
```kotlin
class Account private constructor(
    props: AccountProps,
    id: UniqueEntityId?
) : AggregateRoot<AccountProps>(props, id) {

    fun deposit(amount: Money): Result<Account> {
        if (amount.value <= 0) {
            return Result.failure(UseCaseError("INVALID_DEPOSIT", "Deposit amount must be positive"))
        }

        val newBalance = props.balance.add(amount)
        val newProps = props.copy(balance = newBalance)

        // Publish domain event
        addDomainEvent(AccountDepositedEvent(id, amount.value))

        return Result.success(Account(newProps, id))
    }

    fun withdraw(amount: Money): Result<Account> {
        if (amount.value > props.balance.value) {
            return Result.failure(UseCaseError("INSUFFICIENT_FUNDS", "Insufficient account balance"))
        }

        val newBalance = props.balance.subtract(amount)
        val newProps = props.copy(balance = newBalance)

        addDomainEvent(AccountWithdrawnEvent(id, amount.value))

        return Result.success(Account(newProps, id))
    }
}
```

## Entity Lifecycle

### Creation
```kotlin
// Through factory method
val entity = Entity.create(props, id)
if (entity.isFailure) {
    // Handle creation failure
    return entity
}

// Use the created entity
val createdEntity = entity.getOrThrow()
```

### Modification
```kotlin
// Through business methods
val updatedEntity = existingEntity.updateField(newValue)
if (updatedEntity.isFailure) {
    return updatedEntity
}
```

### Persistence
```kotlin
// Save through repository
val savedEntity = repository.save(updatedEntity.getOrThrow())
```

## Common Patterns

### Identity Generation
```kotlin
class Entity private constructor(
    props: EntityProps,
    id: UniqueEntityId?
) : AggregateRoot<EntityProps>(props, id) {

    val entityId: EntityId
        get() = EntityId.create(_id).getOrThrow()
}
```

### Immutable Updates
```kotlin
fun updateField(newValue: ValueObject): Result<Entity> {
    val newProps = props.copy(field = newValue)
    return create(newProps, id)  // Re-validate through factory
}
```

### Collection Management
```kotlin
class EntityWithCollection private constructor(
    props: EntityWithCollectionProps,
    id: UniqueEntityId?
) : AggregateRoot<EntityWithCollectionProps>(props, id) {

    fun addItem(item: Item): Result<EntityWithCollection> {
        val newItems = props.items.toMutableList().apply { add(item) }
        val newProps = props.copy(items = newItems)
        return create(newProps, id)
    }

    fun removeItem(item: Item): Result<EntityWithCollection> {
        val newItems = props.items.toMutableList().apply { remove(item) }
        val newProps = props.copy(items = newItems)
        return create(newProps, id)
    }
}
```

## Testing Entities

### Unit Test Example
```kotlin
class PlayerTest {

    @Test
    fun `should create player with valid props`() {
        // Given
        val playerName = PlayerName.create("testPlayer").getOrThrow()
        val props = PlayerProps(
            playerName = playerName,
            firstLogin = 1000000L,
            lastLogin = 2000000L,
            totalPlaytimeSeconds = 3600L,
            updateTime = 2000000L,
            statistics = emptyMap(),
            advancements = emptyMap(),
            playerSkin = PlayerSkin.create("default").getOrThrow()
        )

        // When
        val result = Player.create(props, null)

        // Then
        assertTrue(result.isSuccess)
        val player = result.getOrThrow()
        assertEquals("testPlayer", player.playerName.value)
    }

    @Test
    fun `should fail creation with invalid name`() {
        // Given
        val invalidName = PlayerName.create("").getOrThrow() // This would actually fail
        val props = PlayerProps(
            playerName = invalidName,
            // ... other props
        )

        // When
        val result = Player.create(props, null)

        // Then
        assertTrue(result.isFailure)
    }
}
```

These patterns ensure that domain entities are:
- **Immutable**: State changes create new instances
- **Validated**: All business rules enforced at creation/modification
- **Encapsulated**: Business logic contained within entities
- **Event-Sourced**: Domain events published for significant changes
- **Testable**: Easy to unit test with clear boundaries</content>
<parameter name="filePath">/Users/saraki/Documents/project/Wofuf/document/agents/Domain/Entities.md

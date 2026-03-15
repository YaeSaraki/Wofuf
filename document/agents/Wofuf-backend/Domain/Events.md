# Domain Events

This document describes the patterns and conventions for implementing domain events in the Wofuf project using Eventuate Tram.

## Domain Event Pattern

### Basic Event Structure
All domain events implement `IDomainEvent` and follow this pattern:

```kotlin
package dev.saraki.wofuf.modules.example.domain.events

import dev.saraki.wofuf.shared.domain.UniqueEntityId
import dev.saraki.wofuf.shared.domain.events.IDomainEvent
import java.time.LocalDateTime

data class ExampleEvent(
    private val aggregateId: UniqueEntityId,
    val eventData: String,
    val additionalData: Map<String, Any> = emptyMap(),
    override val dataTimeOccurred: LocalDateTime = LocalDateTime.now()
) : IDomainEvent {

    override fun getAggregateId(): UniqueEntityId {
        return aggregateId
    }

    // Optional: Custom methods for event data
    fun getEventData(): String = eventData
}
```

### Key Characteristics

1. **Immutable**: Events are data classes, all properties are read-only
2. **Timestamped**: Include `dataTimeOccurred` for event ordering
3. **Aggregate-bound**: Reference the aggregate that produced the event
4. **Serializable**: Can be persisted and transmitted across services
5. **Descriptive**: Event name clearly indicates what happened

## Eventuate Tram Integration

### Event Publisher Setup
```kotlin
@Configuration
class EventuateTramConfig {

    @Bean
    fun domainEventPublisher(): EventuateTramEventPublisher {
        return EventuateTramEventPublisher()
    }

    @Bean
    fun eventPublisher(eventuateTramEventPublisher: EventuateTramEventPublisher): DomainEventPublisher {
        return EventuateTramDomainEventPublisher(eventuateTramEventPublisher)
    }
}
```

### Publishing Events
```kotlin
@Service
class EventuateTramDomainEventPublisher(
    private val eventuateTramEventPublisher: EventuateTramEventPublisher
) : DomainEventPublisher {

    override fun publish(event: IDomainEvent) {
        eventuateTramEventPublisher.publish(
            event.getAggregateId().uuid.toString(),
            listOf(event)
        )
    }

    override fun publishAll(events: List<IDomainEvent>) {
        events.groupBy { it.getAggregateId() }.forEach { (aggregateId, aggregateEvents) ->
            eventuateTramEventPublisher.publish(
                aggregateId.uuid.toString(),
                aggregateEvents
            )
        }
    }
}
```

## Common Domain Events

### User Events
```kotlin
data class UserCreatedEvent(
    private val aggregateId: UniqueEntityId,
    val userName: String,
    val email: String,
    override val dataTimeOccurred: LocalDateTime = LocalDateTime.now()
) : IDomainEvent {

    override fun getAggregateId(): UniqueEntityId = aggregateId
}

data class UserLoggedInEvent(
    private val aggregateId: UniqueEntityId,
    val loginTime: LocalDateTime,
    val ipAddress: String?,
    override val dataTimeOccurred: LocalDateTime = LocalDateTime.now()
) : IDomainEvent {

    override fun getAggregateId(): UniqueEntityId = aggregateId
}
```

### Forum Events
```kotlin
data class PostCreatedEvent(
    private val aggregateId: UniqueEntityId,
    val postSlug: String,
    val authorId: String,
    val title: String,
    override val dataTimeOccurred: LocalDateTime = LocalDateTime.now()
) : IDomainEvent {

    override fun getAggregateId(): UniqueEntityId = aggregateId
}

data class CommentPostedEvent(
    private val aggregateId: UniqueEntityId,
    val postId: String,
    val authorId: String,
    val parentCommentId: String?,
    val content: String,
    override val dataTimeOccurred: LocalDateTime = LocalDateTime.now()
) : IDomainEvent {

    override fun getAggregateId(): UniqueEntityId = aggregateId
}

data class PostVotesChangedEvent(
    private val aggregateId: UniqueEntityId,
    val postId: String,
    val oldVotes: Int,
    val newVotes: Int,
    val voterId: String,
    override val dataTimeOccurred: LocalDateTime = LocalDateTime.now()
) : IDomainEvent {

    override fun getAggregateId(): UniqueEntityId = aggregateId
}
```

### Player Events
```kotlin
data class PlayerJoinedEvent(
    private val aggregateId: UniqueEntityId,
    val playerName: String,
    val joinTime: Long,
    val serverVersion: String,
    override val dataTimeOccurred: LocalDateTime = LocalDateTime.now()
) : IDomainEvent {

    override fun getAggregateId(): UniqueEntityId = aggregateId
}

data class PlayerStatisticsUpdatedEvent(
    private val aggregateId: UniqueEntityId,
    val playerName: String,
    val updatedStats: Map<String, Int>,
    val updateTime: Long,
    override val dataTimeOccurred: LocalDateTime = LocalDateTime.now()
) : IDomainEvent {

    override fun getAggregateId(): UniqueEntityId = aggregateId
}
```

## Publishing Events in Aggregates

### Basic Event Publishing
```kotlin
class Post private constructor(
    props: PostProps,
    id: UniqueEntityId?
) : AggregateRoot<PostProps>(props, id) {

    fun publishPost(memberId: MemberId): Result<Post> {
        // Business logic validation
        // ...

        // Publish domain event
        addDomainEvent(PostCreatedEvent(
            aggregateId = this.id,
            postSlug = props.slug.value,
            authorId = memberId.stringValue,
            title = props.title.value
        ))

        return Result.success(this)
    }
}
```

### Event Publishing in Business Methods
```kotlin
class Comment private constructor(
    props: CommentProps,
    id: UniqueEntityId?
) : AggregateRoot<CommentProps>(props, id) {

    fun upvote(voterId: String): Result<Comment> {
        val oldPoints = props.points ?: 0
        val newPoints = oldPoints + 1

        val newProps = props.copy(points = newPoints)

        // Publish event
        addDomainEvent(CommentVotesChangedEvent(
            aggregateId = this.id,
            commentId = this.id.uuid.toString(),
            oldPoints = oldPoints,
            newPoints = newPoints,
            voterId = voterId
        ))

        return Result.success(Comment(newProps, id))
    }
}
```

## Event Subscribers

### Basic Subscriber Pattern
```kotlin
@Component
class ExampleEventSubscriber : EventSubscriber() {

    @EventSubscriberMethod
    fun handleExampleEvent(event: ExampleEvent) {
        logger.info("Handling example event: ${event.eventData}")

        // Event handling logic
        // - Update read models
        // - Send notifications
        // - Trigger workflows
        // - Update analytics
    }
}
```

### Complex Event Handling
```kotlin
@Service
class PostEventHandler {

    private val logger = LoggerFactory.getLogger(javaClass)

    @EventHandler
    @Transactional
    fun handlePostCreated(event: PostCreatedEvent) {
        try {
            // Update search index
            searchService.indexPost(event.aggregateId.uuid.toString())

            // Send notifications to followers
            notificationService.notifyFollowers(
                event.authorId,
                "New post: ${event.title}"
            )

            // Update user statistics
            userStatsService.incrementPostCount(event.authorId)

            logger.info("Successfully processed PostCreatedEvent for post: ${event.aggregateId}")
        } catch (e: Exception) {
            logger.error("Failed to process PostCreatedEvent: ${e.message}", e)
            // Consider dead letter queue or retry logic
        }
    }

    @EventHandler
    fun handlePostVotesChanged(event: PostVotesChangedEvent) {
        // Update cached vote counts
        cacheService.updatePostVotes(event.postId, event.newVotes)

        // Check for trending posts
        if (event.newVotes > TRENDING_THRESHOLD) {
            trendingService.addToTrending(event.postId)
        }
    }
}
```

## Event Sourcing (Future Consideration)

### Event Store Pattern
```kotlin
interface EventStore {
    fun saveEvents(aggregateId: String, events: List<IDomainEvent>, expectedVersion: Long)
    fun getEventsForAggregate(aggregateId: String): List<IDomainEvent>
}

class EventSourcedAggregate {
    private val changes: MutableList<IDomainEvent> = mutableListOf()

    protected fun applyChange(event: IDomainEvent) {
        // Apply event to aggregate state
        applyEvent(event)

        // Store event for persistence
        changes.add(event)
    }

    protected abstract fun applyEvent(event: IDomainEvent)

    fun getUncommittedChanges(): List<IDomainEvent> = changes.toList()

    fun markChangesAsCommitted() {
        changes.clear()
    }
}
```

## Testing Domain Events

### Event Publishing Tests
```kotlin
class PostTest {

    @Test
    fun `should publish PostCreatedEvent when post is created`() {
        // Given
        val memberId = MemberId.create(UniqueEntityId()).getOrThrow()
        val postProps = PostProps(
            memberId = memberId,
            slug = PostSlug.create(PostTitle.create("Test Post").getOrThrow()).getOrThrow(),
            title = PostTitle.create("Test Post").getOrThrow(),
            type = PostType.TEXT,
            text = PostText.create("Test content").getOrThrow(),
            link = null,
            comments = Comments.create(),
            votes = PostVotes.create(),
            totalNumComments = 0,
            points = 0,
            dateTimePosted = LocalDateTime.now()
        )

        // When
        val post = Post.create(postProps, null).getOrThrow()

        // Then
        val events = post.domainEvents
        assertEquals(1, events.size)
        assertTrue(events[0] is PostCreatedEvent)
    }
}
```

### Event Handler Tests
```kotlin
@SpringBootTest
class PostEventHandlerTest {

    @Autowired
    lateinit var eventHandler: PostEventHandler

    @MockBean
    lateinit var searchService: SearchService

    @MockBean
    lateinit var notificationService: NotificationService

    @Test
    fun `should index post when PostCreatedEvent is handled`() {
        // Given
        val postId = "post-123"
        val event = PostCreatedEvent(
            aggregateId = UniqueEntityId(postId),
            postSlug = "test-post-123",
            authorId = "user-456",
            title = "Test Post"
        )

        // When
        eventHandler.handlePostCreated(event)

        // Then
        verify(searchService).indexPost(postId)
        verify(notificationService).notifyFollowers(eq("user-456"), any())
    }
}
```

## Event Versioning

### Event Versioning Strategy
```kotlin
interface EventVersion {
    val version: Int
        get() = 1
}

// Versioned event
data class PostCreatedEventV1(
    private val aggregateId: UniqueEntityId,
    val postSlug: String,
    val authorId: String,
    val title: String,
    override val dataTimeOccurred: LocalDateTime = LocalDateTime.now()
) : IDomainEvent, EventVersion {

    override fun getAggregateId(): UniqueEntityId = aggregateId
}

// Upgraded event
data class PostCreatedEventV2(
    private val aggregateId: UniqueEntityId,
    val postSlug: String,
    val authorId: String,
    val title: String,
    val category: String = "general", // New field
    override val dataTimeOccurred: LocalDateTime = LocalDateTime.now()
) : IDomainEvent, EventVersion {

    override val version: Int = 2

    override fun getAggregateId(): UniqueEntityId = aggregateId
}
```

## Best Practices

### Event Design
- **Descriptive Names**: Use past tense verbs (Created, Updated, Deleted)
- **Minimal Data**: Include only essential information
- **Immutable**: Never modify event data after creation
- **Serializable**: Ensure all properties can be serialized
- **Versioned**: Plan for future changes

### Publishing Guidelines
- **Transactional**: Publish events within the same transaction as state changes
- **Idempotent**: Event handlers should be idempotent
- **Ordered**: Events should be processed in order
- **Reliable**: Use Eventuate Tram for guaranteed delivery

### Handler Patterns
- **Single Responsibility**: Each handler should do one thing
- **Error Handling**: Implement proper error handling and logging
- **Performance**: Consider async processing for heavy operations
- **Testing**: Test handlers in isolation and integration

### Monitoring and Observability
```kotlin
@Component
class EventMetrics {

    @Autowired
    lateinit var meterRegistry: MeterRegistry

    @EventHandler
    fun recordEventMetrics(event: IDomainEvent) {
        val eventType = event::class.simpleName
        meterRegistry.counter("domain.events", "type", eventType).increment()
    }
}
```

Domain events enable:
- **Decoupled Communication**: Services communicate through events
- **Audit Trail**: Complete history of state changes
- **Eventual Consistency**: Asynchronous updates across services
- **Scalability**: Event-driven architectures scale better
- **Debugging**: Event logs help trace system behavior</content>
<parameter name="filePath">/Users/saraki/Documents/project/Wofuf/document/agents/Domain/Events.md

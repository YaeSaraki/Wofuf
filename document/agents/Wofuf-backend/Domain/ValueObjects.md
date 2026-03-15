# Value Objects

This document describes the patterns and conventions for implementing value objects in the Wofuf project.

## Value Object Pattern

### Basic Structure
Value objects inherit from `ValueObject<T>` and are immutable:

```kotlin
data class ValueObjectProps(
    val value: PrimitiveType
)

class ValueObject private constructor(
    props: ValueObjectProps
) : dev.saraki.wofuf.shared.domain.ValueObject<ValueObjectProps>(props) {

    val value: PrimitiveType
        get() = props.value

    companion object {
        fun create(value: PrimitiveType): Result<ValueObject> {
            // Validation logic
            if (value.isInvalid()) {
                return Result.failure(UseCaseError("INVALID_VALUE", "Value is invalid"))
            }

            return Result.success(ValueObject(ValueObjectProps(value)))
        }
    }
}
```

### Key Characteristics

1. **Immutable**: No setters, all properties are read-only
2. **Validated**: Created through factory methods with validation
3. **Structural Equality**: Equality based on values, not identity
4. **No Identity**: No unique ID, identified by their values
5. **Replaceable**: Can be completely replaced, not modified

## Common Value Object Types

### String-based Value Objects

#### PlayerName
```kotlin
data class PlayerNameProps(val value: String)

class PlayerName private constructor(
    props: PlayerNameProps
) : ValueObject<PlayerNameProps>(props) {

    val value: String
        get() = props.value

    companion object {
        fun create(name: String): Result<PlayerName> {
            val trimmed = name.trim()

            if (trimmed.isBlank()) {
                return Result.failure(UseCaseError("PLAYER_NAME_EMPTY", "Player name cannot be empty"))
            }

            if (trimmed.length > 16) {
                return Result.failure(UseCaseError("PLAYER_NAME_TOO_LONG", "Player name cannot exceed 16 characters"))
            }

            // Minecraft username validation (letters, numbers, underscores)
            if (!trimmed.matches(Regex("^[a-zA-Z0-9_]+$"))) {
                return Result.failure(UseCaseError("PLAYER_NAME_INVALID", "Player name contains invalid characters"))
            }

            return Result.success(PlayerName(PlayerNameProps(trimmed)))
        }
    }
}
```

#### PostTitle
```kotlin
data class PostTitleProps(val value: String)

class PostTitle private constructor(
    props: PostTitleProps
) : ValueObject<PostTitleProps>(props) {

    val value: String
        get() = props.value

    companion object {
        private const val MAX_LENGTH = 200

        fun create(title: String): Result<PostTitle> {
            val trimmed = title.trim()

            if (trimmed.isBlank()) {
                return Result.failure(UseCaseError("POST_TITLE_EMPTY", "Post title cannot be empty"))
            }

            if (trimmed.length > MAX_LENGTH) {
                return Result.failure(UseCaseError("POST_TITLE_TOO_LONG", "Post title cannot exceed $MAX_LENGTH characters"))
            }

            return Result.success(PostTitle(PostTitleProps(trimmed)))
        }
    }
}
```

#### CommentText
```kotlin
data class CommentTextProps(val value: String)

class CommentText private constructor(
    props: CommentTextProps
) : ValueObject<CommentTextProps>(props) {

    val value: String
        get() = props.value

    companion object {
        private const val MAX_LENGTH = 10000

        fun create(text: String): Result<CommentText> {
            val trimmed = text.trim()

            if (trimmed.isBlank()) {
                return Result.failure(UseCaseError("COMMENT_TEXT_EMPTY", "Comment text cannot be empty"))
            }

            if (trimmed.length > MAX_LENGTH) {
                return Result.failure(UseCaseError("COMMENT_TEXT_TOO_LONG", "Comment text cannot exceed $MAX_LENGTH characters"))
            }

            return Result.success(CommentText(CommentTextProps(trimmed)))
        }
    }
}
```

### Slug-based Value Objects

#### PostSlug
```kotlin
data class PostSlugProps(val value: String)

class PostSlug private constructor(
    props: PostSlugProps
) : ValueObject<PostSlugProps>(props) {

    val value: String
        get() = props.value

    companion object {
        fun create(postTitle: PostTitle): Result<PostSlug> {
            val guardResult = Guard.againstNullOrUndefined(postTitle.value, "PostSlug")
            if (guardResult.isFailure) {
                return Result.failure(guardResult.exceptionOrThrow())
            }

            val randomNumeric = TextUtil.createRandomNumericString(7)
            val titleSlug = postTitle.value.toSlug(SlugConfig())
            val resultSlug = "$titleSlug-$randomNumeric"

            return Result.success(PostSlug(PostSlugProps(resultSlug)))
        }

        fun createFromExisting(slugName: String): Result<PostSlug> {
            val guardResult = Guard.againstNullOrUndefined(slugName, "PostSlug")
            if (guardResult.isFailure) {
                return Result.failure(guardResult.exceptionOrThrow())
            }

            // Validate slug format
            if (!slugName.matches(Regex("^[a-z0-9]+(?:-[a-z0-9]+)*-[0-9]+$"))) {
                return Result.failure(UseCaseError("INVALID_SLUG_FORMAT", "Invalid slug format"))
            }

            return Result.success(PostSlug(PostSlugProps(slugName)))
        }
    }
}
```

### ID-based Value Objects

#### PlayerId
```kotlin
data class PlayerIdProps(val value: UniqueEntityId)

class PlayerId private constructor(
    props: PlayerIdProps
) : ValueObject<PlayerIdProps>(props) {

    val stringValue: String
        get() = props.value.uuid.toString()

    companion object {
        fun create(value: UniqueEntityId): Result<PlayerId> {
            val guardResult = Guard.againstNullOrUndefined(value, "PlayerId")
            if (guardResult.isFailure) {
                return Result.failure(guardResult.getOrThrow())
            }
            return Result.success(PlayerId(PlayerIdProps(value)))
        }
    }
}
```

#### CommentId
```kotlin
data class CommentIdProps(val value: UniqueEntityId)

class CommentId private constructor(
    props: CommentIdProps
) : ValueObject<CommentIdProps>(props) {

    val stringValue: String
        get() = props.value.uuid.toString()

    companion object {
        fun create(value: UniqueEntityId): Result<CommentId> {
            val guardResult = Guard.againstNullOrUndefined(value, "CommentId")
            if (guardResult.isFailure) {
                return Result.failure(guardResult.getOrThrow())
            }
            return Result.success(CommentId(CommentIdProps(value)))
        }
    }
}
```

### Complex Value Objects

#### PlayerSkin
```kotlin
data class PlayerSkinProps(
    val textureValue: String,
    val textureSignature: String
)

class PlayerSkin private constructor(
    props: PlayerSkinProps
) : ValueObject<PlayerSkinProps>(props) {

    val textureValue: String
        get() = props.textureValue

    val textureSignature: String
        get() = props.textureSignature

    companion object {
        fun create(skinData: String): Result<PlayerSkin> {
            if (skinData.isBlank()) {
                return Result.failure(UseCaseError("SKIN_DATA_EMPTY", "Skin data cannot be empty"))
            }

            try {
                // Parse JSON skin data
                val skinJson = JsonParser.parseString(skinData).asJsonObject
                val textures = skinJson.getAsJsonObject("textures")
                val skin = textures.getAsJsonObject("SKIN")
                val textureValue = skin.get("url").asString

                // In a real implementation, you'd validate and decode the texture
                return Result.success(PlayerSkin(PlayerSkinProps(
                    textureValue = textureValue,
                    textureSignature = "" // Would be provided by client
                )))
            } catch (e: Exception) {
                return Result.failure(UseCaseError("INVALID_SKIN_DATA", "Invalid skin data format"))
            }
        }
    }
}
```

#### PlayerStatistic
```kotlin
data class PlayerStatisticProps(
    val type: String,
    val value: Int
)

class PlayerStatistic private constructor(
    props: PlayerStatisticProps
) : ValueObject<PlayerStatisticProps>(props) {

    val type: String
        get() = props.type

    val value: Int
        get() = props.value

    companion object {
        fun create(type: String, value: Int): Result<PlayerStatistic> {
            if (type.isBlank()) {
                return Result.failure(UseCaseError("STATISTIC_TYPE_EMPTY", "Statistic type cannot be empty"))
            }

            if (value < 0) {
                return Result.failure(UseCaseError("STATISTIC_VALUE_NEGATIVE", "Statistic value cannot be negative"))
            }

            return Result.success(PlayerStatistic(PlayerStatisticProps(type, value)))
        }
    }
}
```

## Validation Patterns

### String Validation
```kotlin
companion object {
    fun create(value: String): Result<ValueObject> {
        val trimmed = value.trim()

        // Empty check
        if (trimmed.isBlank()) {
            return Result.failure(UseCaseError("VALUE_EMPTY", "Value cannot be empty"))
        }

        // Length validation
        if (trimmed.length < MIN_LENGTH) {
            return Result.failure(UseCaseError("VALUE_TOO_SHORT", "Value must be at least $MIN_LENGTH characters"))
        }

        if (trimmed.length > MAX_LENGTH) {
            return Result.failure(UseCaseError("VALUE_TOO_LONG", "Value cannot exceed $MAX_LENGTH characters"))
        }

        // Format validation
        if (!trimmed.matches(Regex(PATTERN))) {
            return Result.failure(UseCaseError("VALUE_INVALID_FORMAT", "Value has invalid format"))
        }

        return Result.success(ValueObject(ValueObjectProps(trimmed)))
    }
}
```

### Numeric Validation
```kotlin
companion object {
    fun create(value: Int): Result<ValueObject> {
        // Range validation
        if (value < MIN_VALUE) {
            return Result.failure(UseCaseError("VALUE_TOO_SMALL", "Value must be at least $MIN_VALUE"))
        }

        if (value > MAX_VALUE) {
            return Result.failure(UseCaseError("VALUE_TOO_LARGE", "Value cannot exceed $MAX_VALUE"))
        }

        return Result.success(ValueObject(ValueObjectProps(value)))
    }
}
```

## Utility Functions

### Slug Generation
```kotlin
private data class SlugConfig(
    val replacement: String = "-",
    val symbols: Boolean = false,
    val lower: Boolean = true
)

private fun String.toSlug(config: SlugConfig): String {
    return this
        .let { if (config.lower) it.lowercase() else it }
        .replace(Regex("[^a-zA-Z0-9\\s-]"), "") // Remove special chars except spaces and hyphens
        .trim()
        .replace(Regex("\\s+"), config.replacement) // Replace spaces with replacement char
        .replace(Regex("-+"), config.replacement) // Replace multiple hyphens with single
        .trim(config.replacement.first()) // Remove leading/trailing replacement chars
}
```

### Random String Generation
```kotlin
fun createRandomNumericString(length: Int): String {
    val chars = "0123456789"
    return (1..length)
        .map { chars.random() }
        .joinToString("")
}
```

## Testing Value Objects

### Unit Test Examples
```kotlin
class PlayerNameTest {

    @Test
    fun `should create valid player name`() {
        // Given
        val validName = "testPlayer123"

        // When
        val result = PlayerName.create(validName)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(validName, result.getOrThrow().value)
    }

    @Test
    fun `should reject empty name`() {
        // Given
        val emptyName = ""

        // When
        val result = PlayerName.create(emptyName)

        // Then
        assertTrue(result.isFailure)
        assertEquals("PLAYER_NAME_EMPTY", result.exceptionOrThrow().code)
    }

    @Test
    fun `should reject name with invalid characters`() {
        // Given
        val invalidName = "test@player"

        // When
        val result = PlayerName.create(invalidName)

        // Then
        assertTrue(result.isFailure)
        assertEquals("PLAYER_NAME_INVALID", result.exceptionOrThrow().code)
    }

    @Test
    fun `should reject name that is too long`() {
        // Given
        val longName = "a".repeat(17) // 17 characters

        // When
        val result = PlayerName.create(longName)

        // Then
        assertTrue(result.isFailure)
        assertEquals("PLAYER_NAME_TOO_LONG", result.exceptionOrThrow().code)
    }
}
```

### Equality Testing
```kotlin
@Test
fun `value objects with same values should be equal`() {
    // Given
    val name1 = PlayerName.create("testPlayer").getOrThrow()
    val name2 = PlayerName.create("testPlayer").getOrThrow()

    // Then
    assertEquals(name1, name2)
    assertEquals(name1.hashCode(), name2.hashCode())
}
```

## Common Patterns

### Email Validation
```kotlin
companion object {
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun create(email: String): Result<Email> {
        val trimmed = email.trim().lowercase()

        if (trimmed.isBlank()) {
            return Result.failure(UseCaseError("EMAIL_EMPTY", "Email cannot be empty"))
        }

        if (!trimmed.matches(EMAIL_REGEX)) {
            return Result.failure(UseCaseError("EMAIL_INVALID", "Invalid email format"))
        }

        return Result.success(Email(EmailProps(trimmed)))
    }
}
```

### URL Validation
```kotlin
companion object {
    private val URL_REGEX = Regex("^https?://.*")

    fun create(url: String): Result<Url> {
        val trimmed = url.trim()

        if (trimmed.isBlank()) {
            return Result.failure(UseCaseError("URL_EMPTY", "URL cannot be empty"))
        }

        if (!trimmed.matches(URL_REGEX)) {
            return Result.failure(UseCaseError("URL_INVALID", "URL must start with http:// or https://"))
        }

        return Result.success(Url(UrlProps(trimmed)))
    }
}
```

### Money/Amount Validation
```kotlin
companion object {
    fun create(amount: BigDecimal, currency: Currency = Currency.USD): Result<Money> {
        if (amount < BigDecimal.ZERO) {
            return Result.failure(UseCaseError("AMOUNT_NEGATIVE", "Amount cannot be negative"))
        }

        if (amount.scale() > 2) {
            return Result.failure(UseCaseError("AMOUNT_TOO_PRECISE", "Amount cannot have more than 2 decimal places"))
        }

        return Result.success(Money(MoneyProps(amount, currency)))
    }
}
```

Value objects ensure data integrity by:
- **Validating Input**: All values are validated at creation time
- **Encapsulating Rules**: Business rules are embedded in the value objects
- **Providing Type Safety**: Strong typing prevents primitive obsession
- **Enabling Rich Behavior**: Values can have domain-specific methods
- **Supporting Testing**: Easy to test validation logic in isolation</content>
<parameter name="filePath">/Users/saraki/Documents/project/Wofuf/document/agents/Domain/ValueObjects.md

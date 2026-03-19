# SearchPlayers Use Case Design Document

## 1. Overview

### 1.1 Feature Description
Implement a fuzzy search functionality for players that allows searching by player name or player UUID. The search should return a list of matching players with basic profile information.

### 1.2 Requirements
- **Input**: Search query string (playerName or playerUUID prefix)
- **Output**: List of matching players with basic profile data
- **Constraints**:
  - Minimum query length: 1 character
  - Maximum results: configurable (default 20)
  - Support partial matching for player names
  - Support prefix matching for UUIDs

### 1.3 API Endpoint
```
GET /api/v1/players/search?query={query}&limit={limit}
```

---

## 2. Architecture Design

### 2.1 Layer Structure (DDD)

```
useCases/searchPlayers/
├── SearchPlayersController.kt    # REST API endpoint
├── SearchPlayersUseCase.kt      # Business logic implementation
├── SearchPlayersDto.kt          # Request/Response DTOs
├── SearchPlayersDtoMap.kt       # Domain ↔ DTO mapping
└── SearchPlayersErrors.kt       # Custom error classes
```

### 2.2 Dependencies

```
Controller → UseCase → Repository → JPA Repository
     ↓           ↓          ↓
    DTO      Domain     Entity
```

---

## 3. Domain Layer Design

### 3.1 No New Domain Objects Required
The search functionality uses existing `Player` aggregate and value objects:
- `Player` - Aggregate Root
- `PlayerId` - Value Object
- `PlayerName` - Value Object

### 3.2 Business Rules
1. Query cannot be empty
2. Query must have at least 1 character
3. Results are limited to prevent performance issues
4. Search is case-insensitive for player names

---

## 4. Infrastructure Layer Design

### 4.1 Repository Interface Update

**File:** `PlayerRepo.kt`

```kotlin
interface PlayerRepo {
    // ... existing methods ...
    
    /**
     * Search players by name (fuzzy match) or UUID (prefix match)
     * @param query Search query string
     * @param limit Maximum number of results
     * @return List of matching players
     */
    fun searchByQuery(query: String, limit: Int): List<Player>
}
```

### 4.2 JPA Repository Update

**File:** `PlayerJpaRepo.kt`

```kotlin
interface PlayerJpaRepo : JpaRepository<PlayerEntity, String> {
    // ... existing methods ...
    
    /**
     * Search players by name (case-insensitive, fuzzy match)
     * @param query Search query
     * @param pageable Pagination info
     * @return List of matching player entities
     */
    @Query("SELECT p FROM PlayerEntity p WHERE LOWER(p.playerName) LIKE LOWER(CONCAT('%', :query, '%')) OR p.playerId LIKE CONCAT(:query, '%') ORDER BY p.playerName ASC")
    fun searchByQuery(@Param("query") query: String, pageable: Pageable): List<PlayerEntity>
}
```

### 4.3 Repository Implementation Update

**File:** `PlayerRepoImpl.kt`

```kotlin
@Repository
class PlayerRepoImpl(private val playerJpaRepo: PlayerJpaRepo) : PlayerRepo {
    // ... existing methods ...
    
    override fun searchByQuery(query: String, limit: Int): List<Player> {
        val pageable = PageRequest.of(0, limit)
        return playerJpaRepo.searchByQuery(query, pageable)
            .map(PlayerEntityMapper::toDomain)
    }
}
```

---

## 5. Application Layer Design

### 5.1 SearchPlayersDto.kt

```kotlin
package dev.saraki.wofuf.modules.players.useCases.searchPlayers

class SearchPlayersDto {
    data class Request(
        val query: String,
        val limit: Int = 20
    )
    
    data class Response(
        val players: List<PlayerSummary>
    )
    
    data class PlayerSummary(
        val id: String,
        val name: String,
        val lastLogin: Long
    )
}
```

### 5.2 SearchPlayersUseCase.kt

```kotlin
package dev.saraki.wofuf.modules.players.useCases.searchPlayers

import dev.saraki.wofuf.modules.players.domain.Player
import dev.saraki.wofuf.modules.players.infra.repos.PlayerRepo
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import org.springframework.stereotype.Service

@Service
class SearchPlayersUseCase(
    private val playerRepo: PlayerRepo
) : UseCase<SearchPlayersDto.Request, SearchPlayersDto.Response> {
    
    override fun execute(request: SearchPlayersDto.Request): Result<SearchPlayersDto.Response> {
        // Validation: query cannot be blank
        if (request.query.isBlank()) {
            return SearchPlayersErrors.QueryEmptyError()
        }
        
        // Validation: limit must be positive
        if (request.limit <= 0) {
            return SearchPlayersErrors.InvalidLimitError()
        }
        
        // Cap the limit to prevent abuse
        val effectiveLimit = minOf(request.limit, MAX_RESULTS)
        
        // Execute search
        val players = playerRepo.searchByQuery(request.query.trim(), effectiveLimit)
        
        // Map to DTO
        return Result.success(
            SearchPlayersDto.Response(
                players = players.map { SearchPlayersDtoMap.from(it) }
            )
        )
    }
    
    companion object {
        private const val MAX_RESULTS = 50
    }
}
```

### 5.3 SearchPlayersDtoMap.kt

```kotlin
package dev.saraki.wofuf.modules.players.useCases.searchPlayers

import dev.saraki.wofuf.modules.players.domain.Player

abstract class SearchPlayersDtoMap {
    companion object {
        fun from(player: Player): SearchPlayersDto.PlayerSummary =
            SearchPlayersDto.PlayerSummary(
                id = player.playerId.stringValue,
                name = player.playerName.stringValue,
                lastLogin = player.lastLogin
            )
    }
}
```

### 5.4 SearchPlayersErrors.kt

```kotlin
package dev.saraki.wofuf.modules.players.useCases.searchPlayers

import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCaseError

class SearchPlayersErrors {
    
    class QueryEmptyError : Result.Failure<SearchPlayersDto.Response>(
        exception = UseCaseError(
            code = "QUERY_EMPTY_ERROR",
            message = "Search query cannot be empty"
        )
    )
    
    class InvalidLimitError : Result.Failure<SearchPlayersDto.Response>(
        exception = UseCaseError(
            code = "INVALID_LIMIT_ERROR",
            message = "Limit must be a positive number"
        )
    )
}
```

---

## 6. API Layer Design

### 6.1 API Path Configuration Update

**File:** `PlayerApiPathConfig.kt`

```kotlin
object PlayerApiConstantV1 {
    // ... existing constants ...
    
    object Features {
        // ... existing features ...
        const val SEARCH = "$BASE/search"  // /api/v1/players/search
    }
}
```

### 6.2 SearchPlayersController.kt

```kotlin
package dev.saraki.wofuf.modules.players.useCases.searchPlayers

import dev.saraki.wofuf.modules.players.config.PlayerApiConstantV1
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(PlayerApiConstantV1.Features.SEARCH)
class SearchPlayersController(
    private val searchPlayersUseCase: SearchPlayersUseCase
) : BaseController() {
    
    @GetMapping
    fun searchPlayers(
        @RequestParam query: String,
        @RequestParam(defaultValue = "20") limit: Int
    ): ApiResponse<SearchPlayersDto.Response> {
        val result = searchPlayersUseCase.execute(
            SearchPlayersDto.Request(query, limit)
        ).getOrThrow()
        return ApiResponse.success(result)
    }
}
```

---

## 7. Implementation Checklist

- [ ] Update `PlayerApiPathConfig.kt` - Add SEARCH path
- [ ] Update `PlayerJpaRepo.kt` - Add searchByQuery method
- [ ] Update `PlayerRepo.kt` - Add searchByQuery interface method
- [ ] Update `PlayerRepoImpl.kt` - Implement searchByQuery
- [ ] Create `SearchPlayersDto.kt`
- [ ] Create `SearchPlayersErrors.kt`
- [ ] Create `SearchPlayersDtoMap.kt`
- [ ] Create `SearchPlayersUseCase.kt`
- [ ] Create `SearchPlayersController.kt`
- [ ] Verify compilation

---

## 8. Testing Considerations

### 8.1 Unit Tests
- Test empty query validation
- Test limit validation
- Test successful search with results
- Test search with no results

### 8.2 Integration Tests
- Test API endpoint response format
- Test pagination works correctly
- Test case-insensitive name matching
- Test UUID prefix matching

---

## 9. Performance Considerations

1. **Database Index**: Ensure `playerName` column has an index for efficient LIKE queries
2. **Query Optimization**: Use `LIMIT` in SQL to prevent full table scans
3. **Caching**: Consider caching popular search queries in Redis (future enhancement)
4. **Rate Limiting**: Consider adding rate limiting for search endpoints (future enhancement)

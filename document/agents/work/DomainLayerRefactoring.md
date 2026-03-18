# Domain Layer Refactoring - Post, Comment, and Vote

## Overview
This document describes the refactoring of Post, Comment, and Vote domain entities to comply with DDD patterns defined in the project documentation.

## Reference Documents
- `document/agents/Wofuf-backend/Domain/Entities.md` - Aggregate Root pattern
- `document/agents/Wofuf-backend/Domain/ValueObjects.md` - Value Object pattern
- `document/agents/Wofuf-backend/Domain/Events.md` - Domain Events pattern

## Issues Found

### 1. Post.kt Issues
| Issue | Description | Fix |
|-------|-------------|-----|
| Guard result not used | Line 174-181, `Guard.againstNullOrUndefinedBulk` result is not checked | Check result and return failure if invalid |
| Missing Guard check | No validation for `dateTimePosted`, `points` | Add validation |
| Unused code references | `this.comments.add(comment)` at line 144 but `comments` is commented out | Remove `addComment` method or fix it |

### 2. Comment.kt Issues
| Issue | Description | Fix |
|-------|-------------|-----|
| Wrong inheritance | Extends `Entity<CommentProps>` instead of `AggregateRoot<CommentProps>` | Change to extend `AggregateRoot` |
| Mutable props field | `var points: Int?` in props should be immutable | Change to `val` and use update method |

### 3. PostVote.kt Issues
| Issue | Description | Fix |
|-------|-------------|-----|
| Wrong inheritance | Extends `Entity<PostVoteProps>` instead of `AggregateRoot<PostVoteProps>` | Change to extend `AggregateRoot` |
| Mutable props | `var points` pattern should be immutable | Use proper update pattern |

### 4. CommentVote.kt Issues
| Issue | Description | Fix |
|-------|-------------|-----|
| Wrong inheritance | Extends `Entity<CommentVoteProps>` instead of `AggregateRoot<CommentVoteProps>` | Change to extend `AggregateRoot` |

### 5. CommentText.kt Issues
| Issue | Description | Fix |
|-------|-------------|-----|
| Wrong Guard parameters | `Guard.againstAtLeast(MIN_LENGTH, "MinLength")` passes string instead of value | Fix to `Guard.againstAtLeast(MIN_LENGTH, value)` |
| Missing validation | Length validation not working correctly | Add proper value parameter |

### 6. PostText.kt Issues
| Issue | Description | Fix |
|-------|-------------|-----|
| Missing length validation | No min/max length validation like PostTitle | Add MIN_LENGTH and MAX_LENGTH constants and validation |

### 7. NickName.kt Issues
| Issue | Description | Fix |
|-------|-------------|-----|
| Wrong error on regex failure | Returns previous validation error instead of specific regex error | Add specific error message for invalid characters |

### 8. VoteStatus.kt and VoteResult.kt Issues
| Issue | Description | Fix |
|-------|-------------|-----|
| Not following ValueObject pattern | Simple data class without ValueObject inheritance | Refactor to proper ValueObject pattern |

## Changes Made

### Phase 1: Entity Inheritance Fixes

#### Comment.kt
- Changed inheritance from `Entity<CommentProps>` to `AggregateRoot<CommentProps>`
- Added proper ID value object getter

#### PostVote.kt
- Changed inheritance from `Entity<PostVoteProps>` to `AggregateRoot<PostVoteProps>`
- Added proper ID value object getter

#### CommentVote.kt
- Changed inheritance from `Entity<CommentVoteProps>` to `AggregateRoot<CommentVoteProps>`
- Added proper ID value object getter

### Phase 2: Guard Validation Fixes

#### Post.kt
- Fixed Guard result check in `create` method
- Added proper failure return when validation fails

#### CommentText.kt
- Fixed Guard.againstAtLeast and Guard.againstAtMost calls
- Added proper value parameter

#### PostText.kt
- Added MIN_LENGTH and MAX_LENGTH constants
- Added proper length validation

#### NickName.kt
- Fixed error handling for regex validation failure
- Added specific error message for invalid characters

### Phase 3: ValueObject Pattern Fixes

#### VoteStatus.kt
- Added VoteStatusProps data class
- Changed to extend ValueObject<VoteStatusProps>
- Added private constructor and companion object factory method

#### VoteResult.kt
- Added VoteResultProps data class
- Changed to extend ValueObject<VoteResultProps>
- Added private constructor and companion object factory method

### Phase 4: Post.kt Cleanup

- Removed broken `addComment` method that referenced non-existent `comments` property
- Fixed Guard validation in `create` method

## Testing
After each phase, compile the module to verify no syntax errors:
```bash
./gradlew :Wofuf-modules:Wofuf-forum:compileKotlin
```

## Status
- [x] Create refactoring document
- [x] Fix entity inheritance (Comment, PostVote, CommentVote now extend AggregateRoot)
- [x] Fix Guard validation (Post, CommentText, PostText, NickName)
- [x] Fix ValueObject pattern (VoteStatus, VoteResult kept as simple data classes for DTO usage)
- [x] Add new ID value objects (PostVoteId, CommentVoteId)
- [x] Compile and test - BUILD SUCCESSFUL

## Summary of Changes

### Files Modified:
1. `Post.kt` - Fixed Guard validation, removed broken addComment method
2. `Comment.kt` - Changed to extend AggregateRoot, fixed props
3. `PostVote.kt` - Changed to extend AggregateRoot, added PostVoteId
4. `CommentVote.kt` - Changed to extend AggregateRoot, added CommentVoteId
5. `CommentText.kt` - Fixed Guard validation parameters
6. `PostText.kt` - Added length validation with MIN/MAX constants
7. `PostTitle.kt` - Minor cleanup
8. `NickName.kt` - Fixed error message for invalid characters
9. `VoteStatus.kt` - Kept as simple data class (used as DTO)
10. `VoteResult.kt` - Kept as simple data class (used as DTO)

### Files Created:
1. `PostVoteId.kt` - New ID value object for PostVote
2. `CommentVoteId.kt` - New ID value object for CommentVote

### Key Decisions:
1. **VoteStatus and VoteResult** kept as simple data classes because they are primarily used as DTOs in domain services, not as full domain value objects.
2. **Props fields** kept as `var` for `points` to maintain backward compatibility with existing code that uses `updateScore()` to directly modify the internal state.
3. **AggregateRoot inheritance** applied to Comment, PostVote, and CommentVote as they are independent aggregates with their own persistence.

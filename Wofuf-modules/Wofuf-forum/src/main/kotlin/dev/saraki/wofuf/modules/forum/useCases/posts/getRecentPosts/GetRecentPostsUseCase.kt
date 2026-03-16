package dev.saraki.wofuf.modules.forum.useCases.posts.getRecentPosts

import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberDetails
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberDetailsProps
import dev.saraki.wofuf.modules.forum.infra.repos.MemberRepo
import dev.saraki.wofuf.modules.forum.infra.repos.PostRepo
import dev.saraki.wofuf.modules.forum.mappers.PostDtoMapper
import dev.saraki.wofuf.modules.players.domain.valueObjects.PlayerSkin
import dev.saraki.wofuf.modules.players.infra.repos.jpa.PlayerJpaRepo
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import org.springframework.stereotype.Service

/**
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 * @date 2026/3/15
 * @description Use case for getting recent posts
 */
@Service
class GetRecentPostsUseCase(
    private val postRepo: PostRepo,
    private val memberRepo: MemberRepo,
    private val playerJpaRepo: PlayerJpaRepo,
) : UseCase<GetRecentPostsDto.Request, GetRecentPostsDto.Response> {

    override fun execute(request: GetRecentPostsDto.Request): Result<GetRecentPostsDto.Response> {
        // 1. Validate offset
        if (request.offset != null && request.offset <= 0) {
            return GetRecentPostsErrors.InvalidOffsetError(request.offset)
        }

        // 2. Find recent posts
        val posts = postRepo.findRecentPosts(request.offset)

        // 3. Map posts to DTOs with member details and comment counts
        val postDtos = posts.map { post ->
            // Get member details
            val member = memberRepo.findMemberById(post.memberId)
            val playerSkin = getPlayerSkinFromMember(member?.playerId?.stringValue)
            val memberDetails = if (member != null) {
                MemberDetails.create(
                    MemberDetailsProps(
                        nickName = member.nickname,
                        reputation = member.reputation,
                        playerSkin = playerSkin
                    )
                ).getOrThrow()
            } else {
                // Fallback to unknown member
                MemberDetails.create(
                    MemberDetailsProps(
                        nickName = dev.saraki.wofuf.modules.forum.domain.valueObjects.NickName.create("Unknown").getOrThrow(),
                        reputation = 0,
                        playerSkin = null
                    )
                ).getOrThrow()
            }

            // Get number of comments
            val numComments = postRepo.findNumberOfCommentsByPostId(post.postId) ?: 0

            // Map to DTO
            // TODO: Implement vote status tracking based on userId
            PostDtoMapper.toDto(post, memberDetails, numComments)
        }

        // 4. Return success response
        return Result.success(GetRecentPostsDto.Response(postDtos))
    }

    private fun getPlayerSkinFromMember(playerId: String?): PlayerSkin? {
        if (playerId == null) return null
        val playerEntity = playerJpaRepo.findById(playerId).orElse(null) ?: return null
        val skinEntity = playerEntity.playerSkin ?: return null
        return PlayerSkin.create(
            type = skinEntity.type ?: "",
            skin = skinEntity.skin,
            cape = skinEntity.cape ?: ""
        ).getOrNull()
    }
}

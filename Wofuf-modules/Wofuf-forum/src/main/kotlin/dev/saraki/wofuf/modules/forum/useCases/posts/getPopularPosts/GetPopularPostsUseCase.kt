package dev.saraki.wofuf.modules.forum.useCases.posts.getPopularPosts

import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberDetails
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberDetailsProps
import dev.saraki.wofuf.modules.forum.domain.valueObjects.NickName
import dev.saraki.wofuf.modules.forum.infra.repos.MemberRepo
import dev.saraki.wofuf.modules.forum.infra.repos.PostRepo
import dev.saraki.wofuf.modules.forum.mappers.PostDtoMapper
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import org.springframework.stereotype.Service

/**
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 * @date 2026/3/15
 * @description Use case for getting popular posts
 */
@Service
class GetPopularPostsUseCase(
    private val postRepo: PostRepo,
    private val memberRepo: MemberRepo,
) : UseCase<GetPopularPostsDto.Request, GetPopularPostsDto.Response> {

    override fun execute(request: GetPopularPostsDto.Request): Result<GetPopularPostsDto.Response> {
        // 1. Validate offset
        if (request.offset != null && request.offset <= 0) {
            return GetPopularPostsErrors.InvalidOffsetError(request.offset)
        }

        // 2. Find popular posts
        val posts = postRepo.findPopularPosts(request.offset)

        // 3. Map posts to DTOs with member details and comment counts
        val postDtos = posts.map { post ->
            // Get member details
            val member = memberRepo.findMemberById(post.memberId)
            val memberDetails = if (member != null) {
                MemberDetails.create(
                    MemberDetailsProps(
                        nickName = member.nickname,
                        reputation = member.reputation,
                        playerId = member.playerId
                    )
                ).getOrThrow()
            } else {
                // Fallback to unknown member
                MemberDetails.create(
                    MemberDetailsProps(
                        nickName = NickName.create("Unknown").getOrThrow(),
                        reputation = 0,
                        playerId = null
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
        return Result.success(GetPopularPostsDto.Response(postDtos))
    }
}

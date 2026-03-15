package dev.saraki.wofuf.modules.forum.useCases.posts.getPostBySlug

import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberDetails
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberDetailsProps
import dev.saraki.wofuf.modules.forum.domain.valueObjects.NickName
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostSlug
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
 * @description Use case for getting a post by slug
 */
@Service
class GetPostBySlugUseCase(
    private val postRepo: PostRepo,
    private val memberRepo: MemberRepo,
) : UseCase<GetPostBySlugDto.Request, GetPostBySlugDto.Response> {

    override fun execute(request: GetPostBySlugDto.Request): Result<GetPostBySlugDto.Response> {
        // 1. Validate post slug
        if (request.postSlug.isBlank()) {
            return GetPostBySlugErrors.PostSlugEmptyError()
        }

        // 2. Create PostSlug from existing slug
        val postSlugOrError = PostSlug.createFromExisting(request.postSlug)
        if (postSlugOrError.isFailure) {
            return GetPostBySlugErrors.PostNotFoundError(request.postSlug)
        }
        val postSlug = postSlugOrError.getOrThrow()

        // 3. Find the post by slug
        val post = postRepo.findPostBySlug(postSlug)
            ?: return GetPostBySlugErrors.PostNotFoundError(request.postSlug)

        // 4. Get member details
        val member = memberRepo.findMemberById(post.memberId)
            ?: return GetPostBySlugErrors.MemberNotFoundError(request.postSlug)

        // 5. Create MemberDetails
        val memberDetailsOrError = MemberDetails.create(
            MemberDetailsProps(
                nickName = member.nickname,
                reputation = member.reputation
            )
        )
        if (memberDetailsOrError.isFailure) {
            return GetPostBySlugErrors.MemberNotFoundError(request.postSlug)
        }
        val memberDetails = memberDetailsOrError.getOrThrow()

        // 6. Get number of comments
        val numComments = postRepo.findNumberOfCommentsByPostId(post.postId) ?: 0

        // 7. Map to DTO
        // TODO: Implement vote status tracking based on userId
        val postDto = PostDtoMapper.toDto(post, memberDetails, numComments)

        // 8. Return success response
        return Result.success(GetPostBySlugDto.Response(postDto))
    }
}

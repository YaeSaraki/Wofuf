package dev.saraki.wofuf.modules.forum.useCases.admin.members.getMemberProfile

import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.NickName
import dev.saraki.wofuf.modules.forum.infra.repos.CommentRepo
import dev.saraki.wofuf.modules.forum.infra.repos.MemberRepo
import dev.saraki.wofuf.modules.forum.infra.repos.PostRepo
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import org.springframework.stereotype.Service

@Service
class GetMemberProfileUseCase(
    private val memberRepo: MemberRepo,
    private val postRepo: PostRepo,
    private val commentRepo: CommentRepo,
) : UseCase<GetMemberProfileDto.Request, GetMemberProfileDto.Response> {

    override fun execute(request: GetMemberProfileDto.Request): Result<GetMemberProfileDto.Response> {
        if (request.memberId.isBlank()) {
            return GetMemberProfileErrors.MemberIdEmptyError()
        }

        val memberIdObj = MemberId.create(
            dev.saraki.wofuf.shared.domain.UniqueEntityId(request.memberId)
        ).getOrNull()
            ?: return GetMemberProfileErrors.InvalidMemberIdError(request.memberId)

        val member = memberRepo.findMemberById(memberIdObj)
            ?: return GetMemberProfileErrors.MemberNotFoundByIdError(request.memberId)

        // Get post history
        val posts = postRepo.findPostsByMemberId(member.memberId, request.page, request.size)
        val postSummaries = posts.map { post ->
            GetMemberProfileDto.PostSummary(
                postId = post.postId.stringValue,
                slug = post.slug.value,
                title = post.title.value,
                category = post.category.name,
                points = post.points,
                numComments = post.totalNumComments ?: 0,
                status = post.status.name,
                isPinned = post.isPinned,
                isFeatured = post.isFeatured,
                createdAt = post.dateTimePosted
            )
        }

        // Get comment count
        val commentCount = commentRepo.countCommentsByMemberId(member.memberId)

        // Get joined date from member's createdAt
        val joinedAt = member._createdAt?.toString()

        return Result.success(
            GetMemberProfileDto.Response(
                memberId = member.memberId.stringValue,
                userId = member.userId.stringValue,
                playerId = member.playerId.stringValue,
                nickname = member.nickname.value,
                reputation = member.reputation,
                permissions = member.permissions.map { it.name },
                isBanned = member.isBanned,
                bannedAt = member.bannedAt?.toString(),
                bannedUntil = member.bannedUntil?.toString(),
                bannedReason = member.bannedReason,
                bannedBy = member.bannedBy?.stringValue,
                postHistory = postSummaries,
                totalPosts = postSummaries.size.toLong(),
                commentCount = commentCount,
                joinedAt = joinedAt
            )
        )
    }
}

package dev.saraki.wofuf.modules.forum.useCases.admin.stats

import dev.saraki.wofuf.modules.forum.domain.valueObjects.PermissionPoint
import dev.saraki.wofuf.modules.forum.infra.annotation.RequirePermission
import dev.saraki.wofuf.modules.forum.infra.repos.CommentRepo
import dev.saraki.wofuf.modules.forum.infra.repos.MemberRepo
import dev.saraki.wofuf.modules.forum.infra.repos.PostRepo
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import org.springframework.stereotype.Service

@Service
class GetAdminStatsUseCase(
    private val postRepo: PostRepo,
    private val commentRepo: CommentRepo,
    private val memberRepo: MemberRepo,
) : UseCase<Unit, GetAdminStatsDto.Response> {

    @RequirePermission(PermissionPoint.ADMIN_ACCESS, "Only administrators can view admin stats")
    override fun execute(request: Unit): Result<GetAdminStatsDto.Response> {
        val totalPosts = postRepo.countPosts()
        val totalComments = commentRepo.countAll()
        val totalMembers = memberRepo.countAllMembers()
        val pendingReview = postRepo.countPostsUnderReview()
        val hiddenPosts = postRepo.countHiddenPosts()
        val hiddenComments = commentRepo.countHiddenComments()
        val bannedMembers = memberRepo.countBannedMembers()

        return Result.success(GetAdminStatsDto.Response(
            totalPosts = totalPosts,
            totalComments = totalComments,
            totalMembers = totalMembers,
            pendingReview = pendingReview,
            hiddenPosts = hiddenPosts,
            hiddenComments = hiddenComments,
            bannedMembers = bannedMembers
        ))
    }
}

package dev.saraki.wofuf.modules.forum.useCases.admin.posts.getPostsForReview

import dev.saraki.wofuf.modules.forum.domain.valueObjects.PermissionPoint
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostStatus
import dev.saraki.wofuf.modules.forum.infra.annotation.RequirePermission
import dev.saraki.wofuf.modules.forum.infra.repos.PostRepo
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import org.springframework.stereotype.Service
import java.time.ZoneOffset

@Service
class GetPostsForReviewUseCase(
    private val postRepo: PostRepo,
) : UseCase<GetPostsForReviewDto.Request, GetPostsForReviewDto.Response> {

    @RequirePermission(PermissionPoint.POST_REVIEW, "Only users with POST_REVIEW permission can view posts for review")
    override fun execute(request: GetPostsForReviewDto.Request): Result<GetPostsForReviewDto.Response> {
        val page = request.page.coerceAtLeast(0)
        val size = request.size.coerceIn(1, 100)

        val posts = postRepo.findPostsForReview(page, size)
        val total = postRepo.countByStatus(PostStatus.UNDER_REVIEW)

        val postSummaries = posts.map { post ->
            GetPostsForReviewDto.PostSummary(
                postId = post.postId.stringValue,
                title = post.title.value,
                status = post.status.name,
                dateTimePosted = post.dateTimePosted.toEpochSecond(ZoneOffset.UTC),
                authorId = post.memberId.stringValue
            )
        }

        return Result.success(GetPostsForReviewDto.Response(
            posts = postSummaries,
            total = total,
            page = page,
            size = size
        ))
    }
}

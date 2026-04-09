package dev.saraki.wofuf.modules.forum.useCases.admin.comments.getHiddenComments

import dev.saraki.wofuf.modules.forum.domain.valueObjects.PermissionPoint
import dev.saraki.wofuf.modules.forum.infra.annotation.RequirePermission
import dev.saraki.wofuf.modules.forum.infra.repos.CommentRepo
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import org.springframework.stereotype.Service
import java.time.ZoneOffset

@Service
class GetHiddenCommentsUseCase(
    private val commentRepo: CommentRepo,
) : UseCase<GetHiddenCommentsDto.Request, GetHiddenCommentsDto.Response> {

    @RequirePermission(PermissionPoint.COMMENT_VIEW_HIDDEN, "Only users with COMMENT_VIEW_HIDDEN permission can view hidden comments")
    override fun execute(request: GetHiddenCommentsDto.Request): Result<GetHiddenCommentsDto.Response> {
        val page = request.page.coerceAtLeast(0)
        val size = request.size.coerceIn(1, 100)

        val comments = commentRepo.findHiddenComments(page, size)
        val total = commentRepo.countHiddenComments()

        val commentSummaries = comments.map { comment ->
            GetHiddenCommentsDto.CommentSummary(
                commentId = comment.commentId.stringValue,
                postId = comment.postId.stringValue,
                content = comment.text.value,
                isHidden = comment.isHidden,
                hiddenAt = comment.hiddenAt?.toEpochSecond(ZoneOffset.UTC),
                hiddenBy = comment.hiddenBy?.stringValue,
                authorId = comment.memberId.stringValue
            )
        }

        return Result.success(GetHiddenCommentsDto.Response(
            comments = commentSummaries,
            total = total,
            page = page,
            size = size
        ))
    }
}

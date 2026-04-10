package dev.saraki.wofuf.modules.forum.useCases.admin.comments.getComments

import dev.saraki.wofuf.modules.forum.domain.valueObjects.PermissionPoint
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostId
import dev.saraki.wofuf.modules.forum.infra.annotation.RequirePermission
import dev.saraki.wofuf.modules.forum.infra.repos.CommentRepo
import dev.saraki.wofuf.modules.forum.infra.repos.MemberRepo
import dev.saraki.wofuf.modules.forum.infra.repos.PostRepo
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import org.springframework.stereotype.Service
import java.time.ZoneOffset

@Service
class GetCommentsUseCase(
    private val commentRepo: CommentRepo,
    private val memberRepo: MemberRepo,
    private val postRepo: PostRepo,
) : UseCase<GetCommentsDto.Request, GetCommentsDto.Response> {

    @RequirePermission(PermissionPoint.COMMENT_DELETE_ANY, "Only users with COMMENT_DELETE_ANY permission can view all comments")
    override fun execute(request: GetCommentsDto.Request): Result<GetCommentsDto.Response> {
        val page = request.page.coerceAtLeast(0)
        val size = request.size.coerceIn(1, 100)

        // 获取所有评论（分页）
        val allComments = commentRepo.findAllComments(page, size)
        val total = commentRepo.countAllComments()

        // 过滤隐藏/非隐藏
        val filteredComments = if (request.includeHidden) {
            allComments
        } else {
            allComments.filter { !it.isHidden }
        }

        // 如果有搜索条件，按作者昵称过滤
        val searchFilteredComments = if (!request.search.isNullOrBlank()) {
            val searchLower = request.search.lowercase()
            filteredComments.filter { comment ->
                // 获取评论作者的昵称
                val member = memberRepo.findMemberById(comment.memberId)
                val nickname = member?.nickname?.value?.lowercase() ?: ""
                nickname.contains(searchLower)
            }
        } else {
            filteredComments
        }

        val commentSummaries = searchFilteredComments.map { comment ->
            val member = memberRepo.findMemberById(comment.memberId)
            val post = postRepo.findPostByPostId(comment.postId)
            GetCommentsDto.CommentSummary(
                commentId = comment.commentId.stringValue,
                postId = comment.postId.stringValue,
                postSlug = post?.slug?.value ?: "",
                content = comment.text.value,
                isHidden = comment.isHidden,
                hiddenAt = comment.hiddenAt?.toEpochSecond(ZoneOffset.UTC),
                hiddenBy = comment.hiddenBy?.stringValue,
                authorId = comment.memberId.stringValue,
                authorNickname = member?.nickname?.value ?: "Unknown",
                createdAt = comment._createdAt?.toEpochSecond(ZoneOffset.UTC) ?: 0L
            )
        }

        return Result.success(GetCommentsDto.Response(
            comments = commentSummaries,
            total = if (request.includeHidden) total else total - commentRepo.countHiddenComments(),
            page = page,
            size = size
        ))
    }
}

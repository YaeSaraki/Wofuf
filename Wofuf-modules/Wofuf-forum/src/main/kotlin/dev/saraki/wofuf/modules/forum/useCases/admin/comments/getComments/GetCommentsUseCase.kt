package dev.saraki.wofuf.modules.forum.useCases.admin.comments.getComments

import dev.saraki.wofuf.modules.forum.domain.valueObjects.PermissionPoint
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

        val hasContentSearch = !request.contentSearch.isNullOrBlank()
        val hasAuthorSearch = !request.search.isNullOrBlank()

        // 如果有内容搜索，使用数据库 LIKE 查询
        val (comments, total) = if (hasContentSearch) {
            val contentSearch = request.contentSearch!!
            val foundComments = commentRepo.findCommentsByContentSearch(contentSearch, page, size, request.includeHidden)
            val count = commentRepo.countCommentsByContentSearch(contentSearch, request.includeHidden)
            foundComments to count
        } else {
            // 否则使用原有的分页查询
            val allComments = commentRepo.findAllComments(page, size)
            val totalCount = commentRepo.countAllComments()

            // 过滤隐藏/非隐藏
            val filteredComments = if (request.includeHidden) {
                allComments
            } else {
                allComments.filter { !it.isHidden }
            }
            filteredComments to (if (request.includeHidden) totalCount else totalCount - commentRepo.countHiddenComments())
        }

        // 如果有作者昵称搜索，在结果中过滤
        val searchFilteredComments = if (hasAuthorSearch) {
            val searchLower = request.search!!.lowercase()
            comments.filter { comment ->
                val member = memberRepo.findMemberById(comment.memberId)
                val nickname = member?.nickname?.value?.lowercase() ?: ""
                nickname.contains(searchLower)
            }
        } else {
            comments
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
            total = if (hasAuthorSearch) commentSummaries.size.toLong() else total,
            page = page,
            size = size
        ))
    }
}

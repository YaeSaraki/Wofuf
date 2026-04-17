package dev.saraki.wofuf.modules.forum.useCases.members.getMemberComments

import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
import dev.saraki.wofuf.modules.forum.infra.repos.CommentRepo
import dev.saraki.wofuf.modules.forum.infra.repos.MemberRepo
import dev.saraki.wofuf.modules.forum.infra.repos.PostRepo
import dev.saraki.wofuf.modules.forum.useCases.members.getMemberComments.GetMemberCommentsDto.CommentSummary
import dev.saraki.wofuf.modules.forum.useCases.members.getMemberComments.GetMemberCommentsDto.Request
import dev.saraki.wofuf.modules.forum.useCases.members.getMemberComments.GetMemberCommentsDto.Response
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import org.springframework.stereotype.Service

@Service
class GetMemberCommentsUseCase(
    private val commentRepo: CommentRepo,
    private val memberRepo: MemberRepo,
    private val postRepo: PostRepo,
) : UseCase<Request, Response> {

    override fun execute(request: Request): Result<Response> {
        val page = request.page.coerceAtLeast(0)
        val size = request.size.coerceIn(1, 50)

        val memberIdObj = MemberId.create(UniqueEntityId(request.memberId)).getOrNull()
            ?: return Result.failure("Invalid member ID")

        val member = memberRepo.findMemberById(memberIdObj)
            ?: return Result.failure("Member not found")

        val comments = commentRepo.findCommentsByMemberId(member.memberId, page, size)
        val total = commentRepo.countCommentsByMemberId(member.memberId)

        val commentSummaries = comments.map { comment ->
            val post = postRepo.findPostByPostId(comment.postId)
            CommentSummary(
                commentId = comment.commentId.stringValue,
                postId = comment.postId.stringValue,
                postSlug = post?.slug?.value ?: "",
                postTitle = post?.title?.value ?: "",
                content = comment.text.value,
                createdAt = comment._createdAt?.toString() ?: "",
                points = comment.points
            )
        }

        return Result.success(Response(
            comments = commentSummaries,
            total = total,
            page = page,
            size = size
        ))
    }
}

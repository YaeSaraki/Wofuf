package dev.saraki.wofuf.modules.forum.useCases.comments.replyToPost

import dev.saraki.wofuf.modules.forum.domain.Comment
import dev.saraki.wofuf.modules.forum.domain.CommentProps
import dev.saraki.wofuf.modules.forum.domain.Post
import dev.saraki.wofuf.modules.forum.domain.utils.ShortIdGenerator
import dev.saraki.wofuf.modules.forum.domain.valueObjects.*
import dev.saraki.wofuf.modules.forum.infra.repos.CommentRepo
import dev.saraki.wofuf.modules.forum.infra.repos.MemberRepo
import dev.saraki.wofuf.modules.forum.infra.repos.PostRepo
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import dev.saraki.wofuf.modules.users.domain.valueObjects.UserId
import org.springframework.stereotype.Service

@Service
class ReplyToPostUseCase(
    private val memberRepo: MemberRepo,
    private val postRepo: PostRepo,
    private val commentRepo: CommentRepo,
) : UseCase<ReplyToPostDto.Request, ReplyToPostDto.Response> {
    override fun execute(request: ReplyToPostDto.Request): Result<ReplyToPostDto.Response> {
        if (request.comment.isBlank()) {
            return ReplyToPostErrors.CommentTextEmptyError()
        }

        // 通过 slug 或 postId 查找帖子
        val post: Post = when {
            !request.postSlug.isNullOrBlank() -> {
                val postSlug = PostSlug.createFromExisting(request.postSlug).getOrThrow()
                postRepo.findPostBySlug(postSlug) ?: return ReplyToPostErrors.PostNotFoundError(request.postSlug)
            }
            !request.postId.isNullOrBlank() -> {
                val postIdOrError = PostId.create(UniqueEntityId(request.postId))
                if (postIdOrError.isFailure) {
                    return ReplyToPostErrors.PostNotFoundError(request.postId)
                }
                val postId = postIdOrError.getOrThrow()
                postRepo.findPostByPostId(postId) ?: return ReplyToPostErrors.PostNotFoundError(request.postId)
            }
            else -> {
                return ReplyToPostErrors.PostNotFoundError("null")
            }
        }

        // Get member
        val userIdOrError = UserId.create(UniqueEntityId(request.userId))
        if (userIdOrError.isFailure) {
            return ReplyToPostErrors.MemberNotFoundError(request.userId)
        }
        val userId = userIdOrError.getOrThrow()
        val member = memberRepo.findMemberByUserId(userId) ?: return ReplyToPostErrors.MemberNotFoundError(request.userId)

        // Validate comment text
        val commentTextOrError = CommentText.create(request.comment)
        if (commentTextOrError.isFailure) {
            return ReplyToPostErrors.CommentTextEmptyError()
        }
        val commentText = commentTextOrError.getOrThrow()

        // 生成短 ID
        val shortId = ShortIdGenerator.generateFromString("${post.postId.stringValue}_${System.nanoTime()}")

        // 创建评论 (不再需要 votes)
        val commentProps = CommentProps(
            postId = post.postId,
            text = commentText,
            memberId = member.memberId,
            parentCommentId = null,
            rootCommentId = null,
            shortId = shortId,
            points = 0
        )
        val comment = Comment.create(commentProps).getOrThrow()

        // 直接保存评论到数据库
        commentRepo.save(comment)

        return Result.success(ReplyToPostDto.Response())
    }
}

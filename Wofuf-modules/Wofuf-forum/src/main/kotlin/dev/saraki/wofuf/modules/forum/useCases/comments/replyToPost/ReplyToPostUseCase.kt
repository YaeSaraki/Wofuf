package dev.saraki.wofuf.modules.forum.useCases.comments.replyToPost

import dev.saraki.wofuf.modules.forum.domain.Member
import dev.saraki.wofuf.modules.forum.domain.Post
import dev.saraki.wofuf.modules.forum.domain.valueObjects.*
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
) : UseCase<ReplyToPostDto.Request, ReplyToPostDto.Response> {
    override fun execute(request: ReplyToPostDto.Request): Result<ReplyToPostDto.Response> {
        if (request.comment.isBlank()) {
            return ReplyToPostErrors.CommentTextEmptyError()
        }

        // Validate post ID
        val postIdOrError = PostId.create(UniqueEntityId(request.postId))
        if (postIdOrError.isFailure) {
            return ReplyToPostErrors.PostNotFoundError(request.postId)
        }
        val postId = postIdOrError.getOrThrow()

        // Get post
        val post = postRepo.findPostByPostId(postId) ?: return ReplyToPostErrors.PostNotFoundError(request.postId)

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

        // Add comment directly to post (no parent comment)
        val updatedPost = post.addComment(member.memberId, post.postId, commentText, null)
        if (updatedPost.isFailure) {
            return Result.failure(updatedPost.exceptionOrThrow())
        }

        // Save the updated post
        postRepo.save(updatedPost.getOrThrow())

        return Result.success(ReplyToPostDto.Response())
    }
}

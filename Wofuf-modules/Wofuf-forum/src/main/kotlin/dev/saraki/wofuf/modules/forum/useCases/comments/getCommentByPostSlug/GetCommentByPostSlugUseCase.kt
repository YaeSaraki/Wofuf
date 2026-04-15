package dev.saraki.wofuf.modules.forum.useCases.comments.getCommentByPostSlug

import dev.saraki.wofuf.auth.infra.JwtAuthFilter
import dev.saraki.wofuf.modules.forum.domain.Comment
import dev.saraki.wofuf.modules.forum.domain.services.CommentVoteDomainService
import dev.saraki.wofuf.modules.forum.domain.valueObjects.CommentId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PermissionPoint
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostSlug
import dev.saraki.wofuf.modules.forum.domain.valueObjects.VoteStatus
import dev.saraki.wofuf.modules.forum.dtos.CommentDto
import dev.saraki.wofuf.modules.forum.infra.repos.CommentRepo
import dev.saraki.wofuf.modules.forum.infra.repos.MemberRepo
import dev.saraki.wofuf.modules.forum.infra.repos.PostRepo
import dev.saraki.wofuf.modules.forum.mappers.CommentDtoMapper
import dev.saraki.wofuf.modules.users.domain.valueObjects.UserId
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import org.springframework.stereotype.Service

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/3/15 16:12
 *   @description: Bilibili 风格评论获取 UseCase
 *                 - 主评论（parentCommentId IS NULL）作为根节点
 *                 - 子评论（parentCommentId IS NOT NULL）扁平化展示在同一主评论下
 *                 - 子评论通过 rootCommentId 关联到主评论
 */
@Service
class GetCommentByPostSlugUseCase(
    private val commentRepo: CommentRepo,
    private val postRepo: PostRepo,
    private val memberRepo: MemberRepo,
    private val commentVoteDomainService: CommentVoteDomainService,
) : UseCase<GetCommentByPostSlugDto.Request, GetCommentByPostSlugDto.Response> {
    override fun execute(request: GetCommentByPostSlugDto.Request): Result<GetCommentByPostSlugDto.Response> {
        if (request.postSlug.isBlank()) {
            return GetCommentByPostSlugErrors.PostSlugEmptyError()
        }

        val postSlugOrError = PostSlug.createFromExisting(request.postSlug)
        if (postSlugOrError.isFailure) {
            return GetCommentByPostSlugErrors.PostNotFoundError()
        }
        val postSlug = postSlugOrError.getOrThrow()

        // Verify post exists
        val post = postRepo.findPostBySlug(postSlug) ?: return GetCommentByPostSlugErrors.PostNotFoundError()

        // Check if user can see hidden comments
        val includeHidden = canSeeHiddenComments()

        // 1. 获取所有主评论（parentCommentId IS NULL）
        val rootComments = commentRepo.findRootCommentsByPostSlug(postSlug, includeHidden)

        // 2. 收集所有需要查询的评论ID（包括主评论和子评论）
        val allCommentIds = mutableListOf<CommentId>()
        allCommentIds.addAll(rootComments.map { it.commentId })

        // 3. 为每个主评论获取其子评论
        val childCommentsMap = mutableMapOf<CommentId, List<Comment>>()
        for (rootComment in rootComments) {
            val childComments = commentRepo.findChildCommentsByRootId(rootComment.commentId, includeHidden)
            childCommentsMap[rootComment.commentId] = childComments
            allCommentIds.addAll(childComments.map { it.commentId })
        }

        // 4. 如果是管理员（includeHidden=true），显示所有主评论包括隐藏的
        // 如果是非管理员，需要过滤掉隐藏主评论
        val visibleRootComments = if (includeHidden) {
            rootComments
        } else {
            // 非管理员：过滤掉隐藏主评论
            rootComments.filter { !it.isHidden }
        }

        // 5. 如果是管理员，子评论也全部保留；如果非管理员，过滤掉隐藏的子评论
        val visibleChildCommentsMap = mutableMapOf<CommentId, List<Comment>>()
        for (rootComment in visibleRootComments) {
            val childComments = childCommentsMap[rootComment.commentId] ?: emptyList()
            if (includeHidden) {
                // 管理员：保留所有子评论（包括隐藏的）
                visibleChildCommentsMap[rootComment.commentId] = childComments
            } else {
                // 非管理员：过滤掉隐藏的子评论
                visibleChildCommentsMap[rootComment.commentId] = childComments.filter { !it.isHidden }
            }
        }

        // 重新计算需要查询的评论ID
        val finalAllCommentIds = mutableListOf<CommentId>()
        finalAllCommentIds.addAll(visibleRootComments.map { it.commentId })
        for (rootComment in visibleRootComments) {
            finalAllCommentIds.addAll(visibleChildCommentsMap[rootComment.commentId]?.map { it.commentId } ?: emptyList())
        }

        // 解析用户ID用于查询投票状态
        val memberId = request.userId?.let { MemberId.create(UniqueEntityId(it)).getOrThrow() }

        // 5. 批量获取投票状态
        val voteStatusMap = memberId?.let {
            commentVoteDomainService.getVoteStatuses(finalAllCommentIds, it)
        } ?: emptyMap()

        // 6. 批量获取评论详情
        val commentDetailsMap = commentRepo.findCommentDetailsByCommentIds(finalAllCommentIds)

        // 7. 构建回复者昵称映射（用于显示"回复 @xxx"）
        val replyToNicknameMap = buildReplyToNicknameMap(visibleChildCommentsMap, commentDetailsMap)

        // 8. 构建回复者短 ID 映射（用于点击跳转）
        val replyToShortIdMap = buildReplyToShortIdMap(visibleChildCommentsMap, commentDetailsMap)

        // 9. 构建回复者评论 ID 映射（用于点击跳转定位）
        val replyToParentCommentIdMap = buildReplyToParentCommentIdMap(visibleChildCommentsMap)

        // 10. 构建 DTO 列表
        val commentDtos = visibleRootComments.mapNotNull { rootComment ->
            val rootDetails = commentDetailsMap[rootComment.commentId] ?: return@mapNotNull null
            val rootVoteStatus = voteStatusMap[rootComment.commentId] ?: VoteStatus.empty()

            // 构建子评论 DTO
            val childDtos = visibleChildCommentsMap[rootComment.commentId]?.mapNotNull { childComment ->
                val childDetails = commentDetailsMap[childComment.commentId] ?: return@mapNotNull null
                val childVoteStatus = voteStatusMap[childComment.commentId] ?: VoteStatus.empty()
                val replyToNickname = replyToNicknameMap[childComment.commentId]
                val replyToShortId = replyToShortIdMap[childComment.commentId]
                val replyToParentCommentId = replyToParentCommentIdMap[childComment.commentId]

                CommentDtoMapper.toDto(
                    comment = childComment,
                    commentDetials = childDetails,
                    wasUpvotedByMe = childVoteStatus.wasUpvotedByMe,
                    wasDownvotedByMe = childVoteStatus.wasDownvotedByMe,
                    replyToMemberNickname = replyToNickname,
                    replyToShortId = replyToShortId,
                    replyToParentCommentId = replyToParentCommentId
                )
            } ?: emptyList()

            // 构建主评论 DTO（包含子评论列表）
            CommentDtoMapper.toDto(
                comment = rootComment,
                commentDetials = rootDetails,
                wasUpvotedByMe = rootVoteStatus.wasUpvotedByMe,
                wasDownvotedByMe = rootVoteStatus.wasDownvotedByMe
            ).copy(childComments = childDtos)
        }

        return Result.success(GetCommentByPostSlugDto.Response(commentDtos))
    }

    /**
     * 构建回复者昵称映射
     * key: 子评论ID
     * value: 被回复者的昵称
     */
    private fun buildReplyToNicknameMap(
        childCommentsMap: Map<CommentId, List<Comment>>,
        commentDetailsMap: Map<CommentId, dev.saraki.wofuf.modules.forum.domain.valueObjects.CommentDetails>
    ): Map<CommentId, String> {
        val result = mutableMapOf<CommentId, String>()

        for ((_, childComments) in childCommentsMap) {
            for (childComment in childComments) {
                val parentCommentId = childComment.parentCommentId ?: continue
                val parentDetails = commentDetailsMap[parentCommentId]
                if (parentDetails != null) {
                    result[childComment.commentId] = parentDetails.memberDetails.nickName.value
                }
            }
        }

        return result
    }

    /**
     * 构建回复者短 ID 映射
     * key: 子评论ID
     * value: 被回复者的短 ID（用于点击跳转）
     */
    private fun buildReplyToShortIdMap(
        childCommentsMap: Map<CommentId, List<Comment>>,
        commentDetailsMap: Map<CommentId, dev.saraki.wofuf.modules.forum.domain.valueObjects.CommentDetails>
    ): Map<CommentId, String> {
        val result = mutableMapOf<CommentId, String>()

        for ((_, childComments) in childCommentsMap) {
            for (childComment in childComments) {
                val parentCommentId = childComment.parentCommentId ?: continue
                val parentDetails = commentDetailsMap[parentCommentId]
                if (parentDetails != null) {
                    result[childComment.commentId] = parentDetails.shortId ?: ""
                }
            }
        }

        return result
    }

    /**
     * 构建回复者评论 ID 映射
     * key: 子评论ID
     * value: 被回复者的评论 ID（用于点击跳转定位）
     */
    private fun buildReplyToParentCommentIdMap(
        childCommentsMap: Map<CommentId, List<Comment>>
    ): Map<CommentId, String> {
        val result = mutableMapOf<CommentId, String>()

        for ((_, childComments) in childCommentsMap) {
            for (childComment in childComments) {
                val parentCommentId = childComment.parentCommentId ?: continue
                result[childComment.commentId] = parentCommentId.stringValue
            }
        }

        return result
    }

    /**
     * 检查当前用户是否有权限查看隐藏评论
     */
    private fun canSeeHiddenComments(): Boolean {
        if (!JwtAuthFilter.isAuthenticated()) {
            return false
        }

        val userId = JwtAuthFilter.getCurrentUserId()

        if (userId.isNullOrBlank() || userId == "anonymousUser") {
            return false
        }

        if (JwtAuthFilter.isAdmin()) {
            return true
        }

        val userIdObj = UserId.create(UniqueEntityId(userId)).getOrNull() ?: return false
        val member = memberRepo.findMemberByUserId(userIdObj) ?: return false

        return member.hasPermission(PermissionPoint.COMMENT_VIEW_HIDDEN)
    }
}

package dev.saraki.wofuf.modules.forum.useCases.posts.editPost

import dev.saraki.wofuf.auth.infra.JwtAuthFilter
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PermissionPoint
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostCategory
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostLink
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostLinkProps
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostText
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostTitle
import dev.saraki.wofuf.modules.forum.infra.repos.MemberRepo
import dev.saraki.wofuf.modules.forum.infra.repos.PostRepo
import dev.saraki.wofuf.modules.forum.infra.storage.MarkdownImageUtils
import dev.saraki.wofuf.modules.users.domain.valueObjects.UserId
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import org.springframework.stereotype.Service

/**
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 * @date 2026/3/15
 * @description Use case for editing an existing post
 */
@Service
class EditPostUseCase(
    private val postRepo: PostRepo,
    private val memberRepo: MemberRepo,
) : UseCase<EditPostDto.Request, EditPostDto.Response> {

    override fun execute(request: EditPostDto.Request): Result<EditPostDto.Response> {
        // 1. 验证用户已登录
        if (request.currentUserId.isNullOrBlank()) {
            return EditPostErrors.UnauthorizedError()
        }

        val userIdOrError = UserId.create(UniqueEntityId(request.currentUserId!!))
        if (userIdOrError.isFailure) {
            return EditPostErrors.UnauthorizedError()
        }
        val userId = userIdOrError.getOrThrow()

        // 2. 验证 post ID
        if (request.postId.isBlank()) {
            return EditPostErrors.PostIdEmptyError()
        }

        // 3. 检查至少有一个字段需要更新
        if (request.title == null && request.text == null && request.link == null && request.category == null) {
            return EditPostErrors.NoUpdateDataError()
        }

        // 4. 验证并创建 PostId
        val postIdOrError = PostId.create(UniqueEntityId(request.postId))
        if (postIdOrError.isFailure) {
            return EditPostErrors.PostNotFoundError(request.postId)
        }
        val postId = postIdOrError.getOrThrow()

        // 5. 查找帖子
        val post = postRepo.findPostByPostId(postId)
            ?: return EditPostErrors.PostNotFoundError(request.postId)

        // 6. 获取当前用户的 member
        val member = memberRepo.findMemberByUserId(userId)
            ?: return EditPostErrors.MemberNotFoundError()

        // 7. 检查权限：是帖子作者或管理员（JWT claim 来自 users 服务）
        val isAuthor = post.memberId.stringValue == member.memberId.stringValue
        val isAdmin = JwtAuthFilter.isAdmin()

        if (!isAuthor && !isAdmin) {
            return EditPostErrors.ForbiddenError()
        }

        // 8. 验证并创建值对象
        var newTitle: PostTitle? = null
        var newText: PostText? = null
        var newLink: PostLink? = null
        var newCategory: PostCategory? = null

        if (request.title != null) {
            val titleOrError = PostTitle.create(request.title)
            if (titleOrError.isFailure) {
                return EditPostErrors.InvalidTitleError(request.title)
            }
            newTitle = titleOrError.getOrThrow()
        }

        if (request.text != null) {
            val (isValid, imageCount) = MarkdownImageUtils.validateImageCount(request.text)
            if (!isValid) {
                return EditPostErrors.TooManyImagesError(imageCount, MarkdownImageUtils.MAX_IMAGES_PER_POST)
            }

            val textOrError = PostText.create(request.text)
            if (textOrError.isFailure) {
                return EditPostErrors.InvalidTextError("Text validation failed")
            }
            newText = textOrError.getOrThrow()
        }

        if (request.link != null) {
            val linkOrError = PostLink.create(PostLinkProps(request.link))
            if (linkOrError.isFailure) {
                return EditPostErrors.InvalidLinkError(request.link)
            }
            newLink = linkOrError.getOrThrow()
        }

        if (request.category != null) {
            newCategory = PostCategory.fromString(request.category)
        }

        // 9. 编辑帖子
        val editResult = post.edit(
            title = newTitle,
            text = newText,
            link = newLink,
            category = newCategory
        )
        if (editResult.isFailure) {
            return EditPostErrors.UpdateFailedError(request.postId)
        }
        val editedPost = editResult.getOrThrow()

        // 10. 保存更新
        val savedPost = postRepo.save(editedPost)

        // 11. 返回成功响应
        return Result.success(
            EditPostDto.Response(
                postId = savedPost.postId.stringValue,
                title = savedPost.title.value,
                text = savedPost.text?.value,
                link = savedPost.link?.value,
                category = savedPost.category.name,
                success = true
            )
        )
    }
}

package dev.saraki.wofuf.modules.forum.useCases.posts.createPost

import dev.saraki.wofuf.modules.forum.domain.Post
import dev.saraki.wofuf.modules.forum.domain.valueObjects.*
import dev.saraki.wofuf.modules.forum.infra.repos.MemberRepo
import dev.saraki.wofuf.modules.forum.infra.repos.PostRepo
import dev.saraki.wofuf.modules.forum.infra.storage.MarkdownImageUtils
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import dev.saraki.wofuf.modules.users.domain.valueObjects.UserId
import org.springframework.stereotype.Service
import java.time.LocalDateTime

/**
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 * @date 2026/3/15 16:15
 * @description Create a new post
 */
@Service
class CreatePostUseCase(
    private val memberRepo: MemberRepo,
    private val postRepo: PostRepo,
) : UseCase<Pair<String, CreatePostDto.Request>, CreatePostDto.Response> {
    override fun execute(request: Pair<String, CreatePostDto.Request>): Result<CreatePostDto.Response> {
        val (currentUserId, dto) = request

        if (currentUserId.isBlank()) {
            return CreatePostErrors.UserIdEmptyError()
        }
        if (dto.title.isBlank()) {
            return CreatePostErrors.TitleEmptyError()
        }

        // Validate user ID
        val userIdOrError = UserId.create(UniqueEntityId(currentUserId))
        if (userIdOrError.isFailure) {
            return CreatePostErrors.MemberNotFoundError(currentUserId)
        }
        val userId = userIdOrError.getOrThrow()

        // Get member
        val member = memberRepo.findMemberByUserId(userId) ?: return CreatePostErrors.MemberNotFoundError(currentUserId)

        // Create post title
        val postTitleOrError = PostTitle.create(dto.title)
        if (postTitleOrError.isFailure) {
            return CreatePostErrors.TitleEmptyError()
        }
        val postTitle = postTitleOrError.getOrThrow()

        // Create post slug
        val postSlugOrError = PostSlug.create(postTitle)
        if (postSlugOrError.isFailure) {
            return CreatePostErrors.PostCreationFailedError()
        }
        val postSlug = postSlugOrError.getOrThrow()

        // Create post type
        val postType = try {
            PostType.valueOf(dto.type.uppercase())
        } catch (e: IllegalArgumentException) {
            return CreatePostErrors.TypeInvalidError(dto.type)
        }

        // Create post text if provided
        var postText: PostText? = null
        if (!dto.text.isNullOrBlank()) {
            // 验证图片数量
            val (isValid, imageCount) = MarkdownImageUtils.validateImageCount(dto.text)
            if (!isValid) {
                return CreatePostErrors.TooManyImagesError(imageCount, MarkdownImageUtils.MAX_IMAGES_PER_POST)
            }

            val postTextOrError = PostText.create(dto.text)
            if (postTextOrError.isFailure) {
                return CreatePostErrors.PostCreationFailedError()
            }
            postText = postTextOrError.getOrThrow()
        }

        // Create post link if provided
        var postLink: PostLink? = null
        if (!dto.link.isNullOrBlank()) {
            val postLinkOrError = PostLink.create(PostLinkProps(dto.link))
            if (postLinkOrError.isFailure) {
                return CreatePostErrors.PostCreationFailedError()
            }
            postLink = postLinkOrError.getOrThrow()
        }

        // Create post props (不再需要 comments 和 votes)
        val postProps = dev.saraki.wofuf.modules.forum.domain.PostProps(
            memberId = member.memberId,
            slug = postSlug,
            title = postTitle,
            type = postType,
            text = postText,
            link = postLink,
            totalNumComments = 0,
            points = 0,
            dateTimePosted = LocalDateTime.now()
        )

        // Create post
        val postOrError = Post.create(postProps)
        if (postOrError.isFailure) {
            return CreatePostErrors.PostCreationFailedError()
        }
        val post = postOrError.getOrThrow()

        // Save post
        val savedPost = postRepo.save(post)

        return Result.success(
            CreatePostDto.Response(
                postId = savedPost.postId.stringValue,
                slug = savedPost.slug.value,
            )
        )
    }
}

package dev.saraki.wofuf.modules.forum.useCases.posts.createPost

import dev.saraki.wofuf.modules.forum.domain.Comments
import dev.saraki.wofuf.modules.forum.domain.Post
import dev.saraki.wofuf.modules.forum.domain.PostVotes
import dev.saraki.wofuf.modules.forum.domain.valueObjects.*
import dev.saraki.wofuf.modules.forum.infra.repos.MemberRepo
import dev.saraki.wofuf.modules.forum.infra.repos.PostRepo
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
) : UseCase<CreatePostDto.Request, CreatePostDto.Response> {
    override fun execute(request: CreatePostDto.Request): Result<CreatePostDto.Response> {
        // Debug: 打印请求内容
        println("=== CreatePost Debug ===")
        println("userId: '${request.userId}'")
        println("title: '${request.title}'")
        println("type: '${request.type}'")
        println("text: '${request.text}'")
        println("link: '${request.link}'")
        println("========================")
        
        if (request.userId.isBlank()) {
            return CreatePostErrors.UserIdEmptyError()
        }
        if (request.title.isBlank()) {
            return CreatePostErrors.TitleEmptyError()
        }

        // Validate user ID
        val userIdOrError = UserId.create(UniqueEntityId(request.userId))
        if (userIdOrError.isFailure) {
            return CreatePostErrors.MemberNotFoundError(request.userId)
        }
        val userId = userIdOrError.getOrThrow()

        // Get member
        val member = memberRepo.findMemberByUserId(userId) ?: return CreatePostErrors.MemberNotFoundError(request.userId)

        // Create post title
        val postTitleOrError = PostTitle.create(request.title)
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
            PostType.valueOf(request.type.uppercase())
        } catch (e: IllegalArgumentException) {
            return CreatePostErrors.TypeInvalidError(request.type)
        }

        // Create post text if provided
        var postText: PostText? = null
        if (!request.text.isNullOrBlank()) {
            val postTextOrError = PostText.create(request.text)
            if (postTextOrError.isFailure) {
                return CreatePostErrors.PostCreationFailedError()
            }
            postText = postTextOrError.getOrThrow()
        }

        // Create post link if provided
        var postLink: PostLink? = null
        if (!request.link.isNullOrBlank()) {
            val postLinkOrError = PostLink.create(PostLinkProps(request.link))
            if (postLinkOrError.isFailure) {
                return CreatePostErrors.PostCreationFailedError()
            }
            postLink = postLinkOrError.getOrThrow()
        }

        // Create post props
        val postProps = dev.saraki.wofuf.modules.forum.domain.PostProps(
            memberId = member.memberId,
            slug = postSlug,
            title = postTitle,
            type = postType,
            text = postText,
            link = postLink,
            comments = Comments.create(),
            votes = PostVotes.create(),
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

package dev.saraki.wofuf.modules.forum.useCases.posts.editPost

import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostLink
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostLinkProps
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostText
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostTitle
import dev.saraki.wofuf.modules.forum.infra.repos.PostRepo
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
) : UseCase<EditPostDto.Request, EditPostDto.Response> {

    override fun execute(request: EditPostDto.Request): Result<EditPostDto.Response> {
        // 1. Validate post ID
        if (request.postId.isBlank()) {
            return EditPostErrors.PostIdEmptyError()
        }

        // 2. Check if at least one field is provided for update
        if (request.title == null && request.text == null && request.link == null) {
            return EditPostErrors.NoUpdateDataError()
        }

        // 3. Validate and create PostId
        val postIdOrError = PostId.create(UniqueEntityId(request.postId))
        if (postIdOrError.isFailure) {
            return EditPostErrors.PostNotFoundError(request.postId)
        }
        val postId = postIdOrError.getOrThrow()

        // 4. Find the post
        val post = postRepo.findPostByPostId(postId)
            ?: return EditPostErrors.PostNotFoundError(request.postId)

        // 5. Validate and create value objects for updates
        var newTitle: PostTitle? = null
        var newText: PostText? = null
        var newLink: PostLink? = null

        // Validate title if provided
        if (request.title != null) {
            val titleOrError = PostTitle.create(request.title)
            if (titleOrError.isFailure) {
                return EditPostErrors.InvalidTitleError(request.title)
            }
            newTitle = titleOrError.getOrThrow()
        }

        // Validate text if provided
        if (request.text != null) {
            val textOrError = PostText.create(request.text)
            if (textOrError.isFailure) {
                return EditPostErrors.InvalidTextError("Text validation failed")
            }
            newText = textOrError.getOrThrow()
        }

        // Validate link if provided
        if (request.link != null) {
            val linkOrError = PostLink.create(PostLinkProps(request.link))
            if (linkOrError.isFailure) {
                return EditPostErrors.InvalidLinkError(request.link)
            }
            newLink = linkOrError.getOrThrow()
        }

        // 6. Edit the post
        val editResult = post.edit(
            title = newTitle,
            text = newText,
            link = newLink
        )
        if (editResult.isFailure) {
            return EditPostErrors.UpdateFailedError(request.postId)
        }
        val editedPost = editResult.getOrThrow()

        // 7. Save the updated post
        val savedPost = postRepo.save(editedPost)

        // 8. Return success response
        return Result.success(
            EditPostDto.Response(
                postId = savedPost.postId.stringValue,
                title = savedPost.title.value,
                text = savedPost.text?.value,
                link = savedPost.link?.value,
                success = true
            )
        )
    }
}

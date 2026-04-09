package dev.saraki.wofuf.modules.forum.infra.repos.jpa.mappers

import dev.saraki.wofuf.modules.forum.domain.PostProps
import dev.saraki.wofuf.modules.forum.domain.Post
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostCategory
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostLink
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostLinkProps
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostSlug
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostStatus
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostText
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostTitle
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostType
import dev.saraki.wofuf.modules.forum.infra.repos.jpa.entities.PostEntity
import dev.saraki.wofuf.shared.core.Guard
import dev.saraki.wofuf.shared.domain.UniqueEntityId

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/8 22:43
 *   @description:
 */
object PostEntityMapper {

    fun toDomain(entity: PostEntity): Post {
        // Debug logging for management fields
        println("[PostEntityMapper] Entity ${entity.postId}: isPinned=${entity.isPinned}, isFeatured=${entity.isFeatured}, status=${entity.status}")
        
        val guardResult = Guard.againstNullOrUndefinedBulk(
            listOf(
                Guard.GuardArgument(entity.postId, "postId"),
                Guard.GuardArgument(entity.memberId, "memberId"),
                Guard.GuardArgument(entity.slug, "slug"),
                Guard.GuardArgument(entity.title, "title"),
                Guard.GuardArgument(entity.type, "type"),
                Guard.GuardArgument(entity.dateTimePosted, "dateTimePosted")
            )
        )

        if (guardResult.isFailure) {
            throw guardResult.exceptionOrThrow()
        }

        val postOrError = Post.create(
            props = PostProps(
                memberId = MemberId.create(UniqueEntityId(entity.memberId)).getOrThrow(),
                slug = PostSlug.createFromExisting(entity.slug).getOrThrow(),
                title = PostTitle.create(entity.title).getOrThrow(),
                type = PostType.valueOf(entity.type),
                category = PostCategory.fromString(entity.category) ?: PostCategory.DISCUSSION,
                text = entity.text?.let { PostText.create(it).getOrThrow() },
                link = entity.link?.let { PostLink.create(PostLinkProps(it)).getOrThrow() },
                totalNumComments = entity.totalNumComments,
                points = entity.points,
                dateTimePosted = entity.dateTimePosted,
                // 管理功能相关字段
                status = try { PostStatus.valueOf(entity.status) } catch (e: Exception) { PostStatus.NORMAL },
                isPinned = entity.isPinned,
                isFeatured = entity.isFeatured,
                pinnedAt = entity.pinnedAt,
                featuredAt = entity.featuredAt,
                hiddenAt = entity.hiddenAt,
                hiddenBy = entity.hiddenBy?.let { MemberId.create(UniqueEntityId(it)).getOrThrow() }
            ),
            id = UniqueEntityId(entity.postId)
        )

        val post = postOrError.getOrThrow()

        post._createdAt = entity.createdAt
        post._updatedAt = entity.updatedAt

        return post
    }

    fun toEntity(domain: Post): PostEntity {
        return PostEntity(
            postId = domain.postId.stringValue,
            memberId = domain.memberId.stringValue,
            slug = domain.slug.value,
            title = domain.title.value,
            type = domain.type.toString(),
            category = domain.category.name,
            text = domain.text?.value,
            link = domain.link?.value,
            totalNumComments = domain.totalNumComments ?: 0,
            points = domain.points,
            dateTimePosted = domain.dateTimePosted,
            // 管理功能相关字段
            status = domain.status.name,
            isPinned = domain.isPinned,
            isFeatured = domain.isFeatured,
            pinnedAt = domain.pinnedAt,
            featuredAt = domain.featuredAt,
            hiddenAt = domain.hiddenAt,
            hiddenBy = domain.hiddenBy?.stringValue
        )
    }
}

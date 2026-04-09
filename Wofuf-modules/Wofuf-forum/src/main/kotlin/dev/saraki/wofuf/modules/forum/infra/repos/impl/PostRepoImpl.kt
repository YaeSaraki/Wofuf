package dev.saraki.wofuf.modules.forum.infra.repos.impl

import dev.saraki.wofuf.modules.forum.domain.Post
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostCategory
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostSlug
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostStatus
import dev.saraki.wofuf.modules.forum.infra.repos.PostRepo
import dev.saraki.wofuf.modules.forum.infra.repos.jpa.PostJpaRepo
import dev.saraki.wofuf.modules.forum.infra.repos.jpa.mappers.PostEntityMapper
import org.springframework.stereotype.Repository

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/9 15:30
 *   @description: PostRepo接口的实现类
 */
@Repository
class PostRepoImpl(
    private val postJpaRepo: PostJpaRepo,
) : PostRepo {

    override fun findPostByPostId(postId: PostId): Post? =
        postJpaRepo.findById(postId.stringValue)
            .map(PostEntityMapper::toDomain)
            .orElse(null)

    override fun findNumberOfCommentsByPostId(postId: PostId): Int {
        val postEntity = postJpaRepo.findById(postId.stringValue).orElse(null)
        return postEntity?.comments?.size ?: 0
    }

    override fun findPostBySlug(postSlug: PostSlug): Post? {
        val slug = postSlug.value
        val postEntity = postJpaRepo.findBySlug(slug) ?: return null
        return PostEntityMapper.toDomain(postEntity)
    }

    override fun findRecentPosts(page: Int, size: Int, category: PostCategory?): List<Post> {
        val safeSize = size.coerceAtLeast(1)

        // 不传分类 → 查询全部
        if (category == null) {
            return postJpaRepo.findRecentPosts(page, safeSize)
                .map(PostEntityMapper::toDomain)
        }

        // 传分类 → 只查该分类
        return postJpaRepo.findRecentPostsByCategory(category, page, safeSize)
            .map(PostEntityMapper::toDomain)
    }

    override fun findPopularPosts(page: Int, size: Int, category: PostCategory?): List<Post> {
        val safeSize = size.coerceAtLeast(1)

        // 不传分类 → 查询全部
        if (category == null) {
            return postJpaRepo.findPopularPosts(page, safeSize)
                .map(PostEntityMapper::toDomain)
        }

        // 传分类 → 只查该分类
        return postJpaRepo.findPopularPostsByCategory(category, page, safeSize)
            .map(PostEntityMapper::toDomain)
    }

    override fun exists(postId: PostId): Boolean =
        postJpaRepo.existsById(postId.stringValue)

    override fun save(post: Post): Post {
        // 查找现有实体
        val existingEntity = postJpaRepo.findById(post.postId.stringValue).orElse(null)
        
        val entity = if (existingEntity != null) {
            // 更新现有实体的可变字段
            existingEntity.status = post.status.name
            existingEntity.isPinned = post.isPinned
            existingEntity.isFeatured = post.isFeatured
            existingEntity.pinnedAt = post.pinnedAt
            existingEntity.featuredAt = post.featuredAt
            existingEntity.hiddenAt = post.hiddenAt
            existingEntity.hiddenBy = post.hiddenBy?.stringValue
            existingEntity
        } else {
            // 创建新实体
            PostEntityMapper.toEntity(post)
        }
        
        return PostEntityMapper.toDomain(postJpaRepo.save(entity))
    }

    override fun delete(postId: PostId) =
        postJpaRepo.deleteById(postId.stringValue)

    // ==================== 管理功能方法实现 ====================

    override fun findPinnedPosts(limit: Int): List<Post> =
        postJpaRepo.findByIsPinnedTrueOrderByPinnedAtDesc(limit)
            .map(PostEntityMapper::toDomain)

    override fun findFeaturedPosts(limit: Int): List<Post> =
        postJpaRepo.findByIsFeaturedTrueOrderByFeaturedAtDesc(limit)
            .map(PostEntityMapper::toDomain)

    override fun findPostsByStatus(status: PostStatus, page: Int, size: Int): List<Post> =
        postJpaRepo.findByStatusOrderByDateTimePostedDesc(status.name, page, size)
            .map(PostEntityMapper::toDomain)

    override fun findPostsForReview(page: Int, size: Int): List<Post> =
        postJpaRepo.findByStatusOrderByDateTimePostedDesc(PostStatus.UNDER_REVIEW.name, page, size)
            .map(PostEntityMapper::toDomain)

    override fun countByStatus(status: PostStatus): Long =
        postJpaRepo.countByStatus(status.name)
}

package dev.saraki.wofuf.modules.forum.infra.repos.impl

import dev.saraki.wofuf.modules.forum.domain.Post
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostCategory
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostSlug
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostStatus
import dev.saraki.wofuf.modules.forum.infra.repos.PostRepo
import dev.saraki.wofuf.modules.forum.infra.repos.jpa.PostJpaRepo
import dev.saraki.wofuf.modules.forum.infra.repos.jpa.mappers.PostEntityMapper
import org.springframework.data.domain.PageRequest
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

    override fun findRecentPosts(page: Int, size: Int, category: PostCategory?, includeHidden: Boolean): List<Post> {
        val safeSize = size.coerceAtLeast(1)

        // JPA 查询获取所有帖子（不过滤状态）
        val allPosts = if (category == null) {
            postJpaRepo.findRecentPosts(page, safeSize)
        } else {
            postJpaRepo.findRecentPostsByCategory(category, page, safeSize)
        }

        // Repository 层根据 includeHidden 决定是否过滤隐藏帖子
        // 注意：这是基础设施层的最低限度业务逻辑，用于数据可见性控制
        val filteredPosts = if (includeHidden) {
            allPosts
        } else {
            allPosts.filter { it.status == "NORMAL" }
        }

        return filteredPosts.map(PostEntityMapper::toDomain)
    }

    override fun findPopularPosts(page: Int, size: Int, category: PostCategory?, includeHidden: Boolean): List<Post> {
        val safeSize = size.coerceAtLeast(1)

        // JPA 查询获取所有帖子（不过滤状态）
        val allPosts = if (category == null) {
            postJpaRepo.findPopularPosts(page, safeSize)
        } else {
            postJpaRepo.findPopularPostsByCategory(category, page, safeSize)
        }

        // Repository 层根据 includeHidden 决定是否过滤隐藏帖子
        // 注意：这是基础设施层的最低限度业务逻辑，用于数据可见性控制
        val filteredPosts = if (includeHidden) {
            allPosts
        } else {
            allPosts.filter { it.status == "NORMAL" }
        }

        return filteredPosts.map(PostEntityMapper::toDomain)
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

    override fun findPostsByMemberId(memberId: MemberId, page: Int, size: Int): List<Post> =
        postJpaRepo.findByMemberEntity_MemberIdOrderByDateTimePostedDesc(
            memberId.stringValue,
            org.springframework.data.domain.PageRequest.of(page, size)
        ).map(PostEntityMapper::toDomain)

    // ==================== 统计方法实现 ====================

    override fun countPosts(): Long =
        postJpaRepo.count()

    override fun countPostsUnderReview(): Long =
        postJpaRepo.countByStatus(PostStatus.UNDER_REVIEW.name)

    override fun countHiddenPosts(): Long =
        postJpaRepo.countByStatus(PostStatus.HIDDEN.name)

    // ==================== 搜索功能实现 ====================

    override fun searchPosts(query: String, page: Int, size: Int, category: PostCategory?): List<Post> {
        val safeSize = size.coerceAtLeast(1)
        val pageable = PageRequest.of(page, safeSize)

        val posts = if (category == null) {
            postJpaRepo.searchPosts(query, pageable)
        } else {
            postJpaRepo.searchPostsByCategory(query, category.name, pageable)
        }

        // 只返回正常状态的帖子
        return posts
            .filter { it.status == PostStatus.NORMAL.name }
            .map(PostEntityMapper::toDomain)
    }
}

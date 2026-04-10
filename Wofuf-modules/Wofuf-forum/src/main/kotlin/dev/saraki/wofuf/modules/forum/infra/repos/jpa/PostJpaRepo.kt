package dev.saraki.wofuf.modules.forum.infra.repos.jpa

import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostCategory
import dev.saraki.wofuf.modules.forum.infra.repos.jpa.entities.PostEntity
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository

interface PostJpaRepo : JpaRepository<PostEntity, String> {

    fun findBySlug(slug: String): PostEntity?
    fun findPostEntityByLink(link: String): PostEntity?
    fun countByPostId(postId: String): Long

    fun findAllByOrderByDateTimePostedDescPointsDesc(pageable: Pageable): List<PostEntity>
    fun findByCategoryOrderByDateTimePostedDescPointsDesc(category: String, pageable: Pageable): List<PostEntity>
    fun findByStatusOrderByDateTimePostedDescPointsDesc(status: String, pageable: Pageable): List<PostEntity>
    fun findByStatusAndCategoryOrderByDateTimePostedDescPointsDesc(status: String, category: String, pageable: Pageable): List<PostEntity>

    fun findAllByOrderByPointsDescDateTimePostedDesc(pageable: Pageable): List<PostEntity>
    fun findByCategoryOrderByPointsDescDateTimePostedDesc(category: String, pageable: Pageable): List<PostEntity>
    fun findByStatusOrderByPointsDescDateTimePostedDesc(status: String, pageable: Pageable): List<PostEntity>
    fun findByStatusAndCategoryOrderByPointsDescDateTimePostedDesc(status: String, category: String, pageable: Pageable): List<PostEntity>

    fun findRecentPosts(page: Int?, size: Int?): List<PostEntity> {
        val safeSize = size?.coerceAtLeast(1) ?: 10
        val safePage = page ?: 0
        // 置顶帖子优先，然后按时间倒序（不过滤状态，由 Repository/UseCase 层决定）
        val sort = Sort.by(
            Sort.Order.desc("isPinned"),
            Sort.Order.desc("dateTimePosted"),
            Sort.Order.desc("points")
        )
        return findAll(
            PageRequest.of(safePage, safeSize, sort)
        ).content
    }

    fun findRecentPostsByCategory(category: PostCategory, page: Int?, size: Int?): List<PostEntity> {
        val safeSize = size?.coerceAtLeast(1) ?: 10
        val safePage = page ?: 0
        // 置顶帖子优先，然后按时间倒序（不过滤状态，由 Repository/UseCase 层决定）
        val sort = Sort.by(
            Sort.Order.desc("isPinned"),
            Sort.Order.desc("dateTimePosted"),
            Sort.Order.desc("points")
        )
        return findAll(
            PageRequest.of(safePage, safeSize * 2, sort)
        ).content.filter { it.category == category.name }.take(safeSize)
    }

    fun findPopularPosts(page: Int?, size: Int?): List<PostEntity> {
        val safeSize = size?.coerceAtLeast(1) ?: 10
        val safePage = page ?: 0
        // 置顶帖子优先，然后按热度、时间倒序（不过滤状态，由 Repository/UseCase 层决定）
        val sort = Sort.by(
            Sort.Order.desc("isPinned"),
            Sort.Order.desc("points"),
            Sort.Order.desc("dateTimePosted")
        )
        return findAll(
            PageRequest.of(safePage, safeSize, sort)
        ).content
    }

    // 分类热门（不过滤状态，由 Repository/UseCase 层决定）
    fun findPopularPostsByCategory(category: PostCategory, page: Int?, size: Int?): List<PostEntity> {
        val safeSize = size?.coerceAtLeast(1) ?: 10
        val safePage = page ?: 0
        // 置顶帖子优先，然后按热度、时间倒序
        val sort = Sort.by(
            Sort.Order.desc("isPinned"),
            Sort.Order.desc("points"),
            Sort.Order.desc("dateTimePosted")
        )
        return findAll(
            PageRequest.of(safePage, safeSize * 2, sort)
        ).content.filter { it.category == category.name }.take(safeSize)
    }

    // ==================== 管理功能方法 ====================

    fun findByIsPinnedTrueOrderByPinnedAtDesc(limit: Int): List<PostEntity> {
        return findAll(
            PageRequest.of(0, limit, Sort.by(Sort.Order.desc("pinnedAt")))
        ).content.filter { it.isPinned }
    }

    fun findByIsFeaturedTrueOrderByFeaturedAtDesc(limit: Int): List<PostEntity> {
        return findAll(
            PageRequest.of(0, limit, Sort.by(Sort.Order.desc("featuredAt")))
        ).content.filter { it.isFeatured }
    }

    fun findByStatusOrderByDateTimePostedDesc(status: String, page: Int, size: Int): List<PostEntity> {
        return findAll(
            PageRequest.of(page, size, Sort.by(Sort.Order.desc("dateTimePosted")))
        ).content.filter { it.status == status }
    }

    fun countByStatus(status: String): Long {
        return findAll().count { it.status == status }.toLong()
    }

    // ==================== 管理功能：获取所有帖子（不限状态）====================

    /**
     * 获取所有帖子，不限状态，按时间倒序，置顶优先
     */
    fun findAllByOrderByDateTimePostedDesc(page: Int, size: Int): List<PostEntity> {
        return findAll(
            PageRequest.of(page, size, Sort.by(
                Sort.Order.desc("isPinned"),
                Sort.Order.desc("dateTimePosted")
            ))
        ).content
    }

    /**
     * 获取所有帖子，不限状态，按热度排序，置顶优先
     */
    fun findAllByOrderByPointsDescDateTimePostedDesc(page: Int, size: Int): List<PostEntity> {
        return findAll(
            PageRequest.of(page, size, Sort.by(
                Sort.Order.desc("isPinned"),
                Sort.Order.desc("points"),
                Sort.Order.desc("dateTimePosted")
            ))
        ).content
    }

    /**
     * 按分类获取所有帖子，不限状态，置顶优先
     */
    fun findAllByCategoryOrderByDateTimePostedDesc(category: String, page: Int, size: Int): List<PostEntity> {
        return findAll(
            PageRequest.of(page, size * 2, Sort.by(
                Sort.Order.desc("isPinned"),
                Sort.Order.desc("dateTimePosted")
            ))
        ).content.filter { it.category == category }.take(size)
    }

    /**
     * 按分类获取所有帖子，不限状态，按热度排序，置顶优先
     */
    fun findAllByCategoryOrderByPointsDescDateTimePostedDesc(category: String, page: Int, size: Int): List<PostEntity> {
        return findAll(
            PageRequest.of(page, size * 2, Sort.by(
                Sort.Order.desc("isPinned"),
                Sort.Order.desc("points"),
                Sort.Order.desc("dateTimePosted")
            ))
        ).content.filter { it.category == category }.take(size)
    }
}
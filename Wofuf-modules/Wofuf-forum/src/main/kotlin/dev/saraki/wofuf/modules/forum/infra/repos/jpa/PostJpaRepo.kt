package dev.saraki.wofuf.modules.forum.infra.repos.jpa

import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostCategory
import dev.saraki.wofuf.modules.forum.infra.repos.jpa.entities.PostEntity
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository

interface PostJpaRepo : JpaRepository<PostEntity, String> {

    @EntityGraph(attributePaths = ["memberEntity"])
    fun findBySlug(slug: String): PostEntity?

    @EntityGraph(attributePaths = ["memberEntity"])
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

    @EntityGraph(attributePaths = ["memberEntity"])
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

    @org.springframework.data.jpa.repository.Query(
        "SELECT p FROM PostEntity p WHERE p.category = :category ORDER BY p.isPinned DESC, p.dateTimePosted DESC, p.points DESC"
    )
    fun findRecentPostsByCategoryDb(category: String, pageable: PageRequest): List<PostEntity>

    @org.springframework.data.jpa.repository.Query(
        "SELECT p FROM PostEntity p WHERE p.category = :category ORDER BY p.isPinned DESC, p.points DESC, p.dateTimePosted DESC"
    )
    fun findPopularPostsByCategoryDb(category: String, pageable: PageRequest): List<PostEntity>

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
    @EntityGraph(attributePaths = ["memberEntity"])
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

    @EntityGraph(attributePaths = ["memberEntity"])
    fun findByIsPinnedTrueOrderByPinnedAtDesc(limit: Int): List<PostEntity>

    @org.springframework.data.jpa.repository.Query(
        "SELECT p FROM PostEntity p WHERE p.isPinned = true ORDER BY p.pinnedAt DESC"
    )
    fun findPinnedPostsDb(limit: Int): List<PostEntity>

    @EntityGraph(attributePaths = ["memberEntity"])
    fun findByIsFeaturedTrueOrderByFeaturedAtDesc(limit: Int): List<PostEntity>

    @org.springframework.data.jpa.repository.Query(
        "SELECT p FROM PostEntity p WHERE p.isFeatured = true ORDER BY p.featuredAt DESC"
    )
    fun findFeaturedPostsDb(limit: Int): List<PostEntity>

    @org.springframework.data.jpa.repository.Query(
        "SELECT p FROM PostEntity p WHERE p.status = :status ORDER BY p.dateTimePosted DESC"
    )
    fun findByStatusOrderByDateTimePostedDescDb(status: String, pageable: PageRequest): List<PostEntity>

    fun countByStatus(status: String): Long {
        return findAll().count { it.status == status }.toLong()
    }

    @org.springframework.data.jpa.repository.Query(
        "SELECT COUNT(p) FROM PostEntity p WHERE p.status = :status"
    )
    fun countByStatusDb(status: String): Long

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
    @org.springframework.data.jpa.repository.Query(
        "SELECT p FROM PostEntity p WHERE p.category = :category ORDER BY p.isPinned DESC, p.dateTimePosted DESC"
    )
    fun findAllByCategoryOrderByDateTimePostedDescDb(category: String, pageable: PageRequest): List<PostEntity>

    /**
     * 按分类获取所有帖子，不限状态，按热度排序，置顶优先
     */
    @org.springframework.data.jpa.repository.Query(
        "SELECT p FROM PostEntity p WHERE p.category = :category ORDER BY p.isPinned DESC, p.points DESC, p.dateTimePosted DESC"
    )
    fun findAllByCategoryOrderByPointsDescDateTimePostedDescDb(category: String, pageable: PageRequest): List<PostEntity>

    // 按 memberId 查询帖子（管理功能）
    @EntityGraph(attributePaths = ["memberEntity"])
    fun findByMemberEntity_MemberIdOrderByDateTimePostedDesc(memberId: String, pageable: Pageable): List<PostEntity>

    // ==================== 搜索功能 ====================

    /**
     * 搜索帖子（按标题或内容模糊搜索）
     */
    @org.springframework.data.jpa.repository.Query(
        "SELECT p FROM PostEntity p WHERE " +
        "(LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
        "LOWER(p.text) LIKE LOWER(CONCAT('%', :query, '%'))) " +
        "ORDER BY p.isPinned DESC, p.dateTimePosted DESC"
    )
    fun searchPosts(query: String, pageable: Pageable): List<PostEntity>

    /**
     * 按分类搜索帖子
     */
    @org.springframework.data.jpa.repository.Query(
        "SELECT p FROM PostEntity p WHERE " +
        "(LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
        "LOWER(p.text) LIKE LOWER(CONCAT('%', :query, '%'))) " +
        "AND p.category = :category " +
        "ORDER BY p.isPinned DESC, p.dateTimePosted DESC"
    )
    fun searchPostsByCategory(query: String, category: String, pageable: Pageable): List<PostEntity>
}
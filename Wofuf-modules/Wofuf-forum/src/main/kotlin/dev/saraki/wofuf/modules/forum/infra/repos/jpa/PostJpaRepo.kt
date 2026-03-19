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

    fun findAllByOrderByPointsDescDateTimePostedDesc(pageable: Pageable): List<PostEntity>
    fun findByCategoryOrderByPointsDescDateTimePostedDesc(category: String, pageable: Pageable): List<PostEntity>

    fun findRecentPosts(page: Int?, size: Int?): List<PostEntity> {
        val safeSize = size?.coerceAtLeast(1) ?: 10
        val safePage = page ?: 0
        return findAllByOrderByDateTimePostedDescPointsDesc(PageRequest.of(safePage, safeSize))
    }

    fun findRecentPostsByCategory(category: PostCategory, page: Int?, size: Int?): List<PostEntity> {
        val safeSize = size?.coerceAtLeast(1) ?: 10
        val safePage = page ?: 0
        return findByCategoryOrderByDateTimePostedDescPointsDesc(
            category.name,
            PageRequest.of(safePage, safeSize)
        )
    }

    fun findPopularPosts(page: Int?, size: Int?): List<PostEntity> {
        val safeSize = size?.coerceAtLeast(1) ?: 10
        val safePage = page ?: 0
        val sort = Sort.by(Sort.Order.desc("points"), Sort.Order.desc("dateTimePosted"))
        return findAllByOrderByPointsDescDateTimePostedDesc(
            PageRequest.of(safePage, safeSize, sort)
        )
    }

    // 分类热门（正常不动）
    fun findPopularPostsByCategory(category: PostCategory, page: Int?, size: Int?): List<PostEntity> {
        val safeSize = size?.coerceAtLeast(1) ?: 10
        val safePage = page ?: 0
        val sort = Sort.by(Sort.Order.desc("points"), Sort.Order.desc("dateTimePosted"))
        return findByCategoryOrderByPointsDescDateTimePostedDesc(
            category.name,
            PageRequest.of(safePage, safeSize, sort)
        )
    }
}
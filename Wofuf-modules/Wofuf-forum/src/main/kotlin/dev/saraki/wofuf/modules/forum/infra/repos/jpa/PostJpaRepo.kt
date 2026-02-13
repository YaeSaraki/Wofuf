package dev.saraki.wofuf.modules.forum.infra.repos.jpa

import dev.saraki.wofuf.modules.forum.infra.repos.jpa.entities.PostEntity
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/9 14:11
 *   @description:
 */
interface PostJpaRepo : JpaRepository<PostEntity, String> {

    fun findBySlug(slug: String): PostEntity?

    fun findPostEntityByLink(link: String): PostEntity?

    fun countByPostId(postId: String): Long

    // 按发布时间降序查帖子
    fun findAllByOrderByDateTimePostedDesc(pageable: Pageable): List<PostEntity>

    // 按点赞数+发布时间降序查热门帖子
    fun findAllByOrderByPointsDescDateTimePostedDesc(pageable: Pageable): List<PostEntity>

    //查前10条最新帖子
    fun findRecentPosts(offset: Int?): List<PostEntity> {
        return findAllByOrderByDateTimePostedDesc(PageRequest.of(0, offset ?: 10))
    }

    fun findPopularPosts(offset: Int?): List<PostEntity> {
        val sort = Sort.by(Sort.Order.desc("points"), Sort.Order.desc("dateTimePosted"))
        return findAllByOrderByPointsDescDateTimePostedDesc(PageRequest.of(0, 10, sort))
    }
}
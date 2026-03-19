package dev.saraki.wofuf.modules.forum.infra.repos.impl

import dev.saraki.wofuf.modules.forum.domain.Post
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostCategory
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostSlug
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
        val entity = PostEntityMapper.toEntity(post)
        return PostEntityMapper.toDomain(postJpaRepo.save(entity))
    }

    override fun delete(postId: PostId) =
        postJpaRepo.deleteById(postId.stringValue)
}

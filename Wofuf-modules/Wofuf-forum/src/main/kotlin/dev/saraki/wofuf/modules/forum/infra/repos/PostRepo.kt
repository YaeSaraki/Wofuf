package dev.saraki.wofuf.modules.forum.infra.repos

import dev.saraki.wofuf.modules.forum.domain.Post
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostCategory
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostSlug
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostStatus

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/9 15:30
 *   @description: Post领域仓储接口
 */
interface PostRepo {
    fun findPostByPostId(postId: PostId): Post?
    fun findPostBySlug(postSlug: PostSlug): Post?
    fun findRecentPosts(page: Int, size: Int, category: PostCategory?, includeHidden: Boolean = false): List<Post>
    fun findPopularPosts(page: Int, size: Int, category: PostCategory?, includeHidden: Boolean = false): List<Post>
    fun findNumberOfCommentsByPostId(postId: PostId): Int?
    fun exists(postId: PostId): Boolean
    fun save(post: Post): Post
    fun delete(postId: PostId)

    // ==================== 管理功能方法 ====================

    /**
     * 获取置顶帖子列表
     */
    fun findPinnedPosts(limit: Int): List<Post>

    /**
     * 获取加精帖子列表
     */
    fun findFeaturedPosts(limit: Int): List<Post>

    /**
     * 根据状态分页获取帖子
     */
    fun findPostsByStatus(status: PostStatus, page: Int, size: Int): List<Post>

    /**
     * 获取待审核帖子
     */
    fun findPostsForReview(page: Int, size: Int): List<Post>

    /**
     * 统计指定状态的帖子数量
     */
    fun countByStatus(status: PostStatus): Long
}

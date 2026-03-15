package dev.saraki.wofuf.modules.forum.infra.repos

import dev.saraki.wofuf.modules.forum.domain.Post
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostSlug

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/9 15:30
 *   @description: Post领域仓储接口
 */
interface PostRepo {
    fun findPostByPostId(postId: PostId): Post?
    fun findPostBySlug(postSlug: PostSlug): Post?
    fun findRecentPosts(offset: Int? = null): List<Post>
    fun findPopularPosts(offset: Int? = null): List<Post>
    fun findNumberOfCommentsByPostId(postId: PostId): Int?
    fun exists(postId: PostId): Boolean
    fun save(post: Post): Post
    fun delete(postId: PostId)
}
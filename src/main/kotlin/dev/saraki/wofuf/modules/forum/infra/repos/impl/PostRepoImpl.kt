package dev.saraki.wofuf.modules.forum.infra.repos.impl

import dev.saraki.wofuf.modules.forum.domain.Post
import dev.saraki.wofuf.modules.forum.domain.PostDetails
import dev.saraki.wofuf.modules.forum.domain.PostId
import dev.saraki.wofuf.modules.forum.domain.PostSlug
import dev.saraki.wofuf.modules.forum.infra.repos.MemberRepo
import dev.saraki.wofuf.modules.forum.infra.repos.PostRepo
import dev.saraki.wofuf.modules.forum.infra.repos.PostVotesRepo
import dev.saraki.wofuf.modules.forum.infra.repos.jpa.PostJpaRepo
import dev.saraki.wofuf.modules.forum.infra.repos.jpa.mappers.PostDetailsMapper
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
    private val memberRepo: MemberRepo,
    private val postVotesRepo: PostVotesRepo,
) : PostRepo {

    override fun findPostByPostId(postId: PostId): Post? =
        postJpaRepo.findById(postId.stringValue)
            .map(PostEntityMapper::toDomain)
            .orElse(null)

    override fun findNumberOfCommentsByPostId(postId: PostId): Int {
        val postEntity = postJpaRepo.findById(postId.stringValue).orElse(null)
        return postEntity.comments.size
    }

    override fun findPostBySlug(postSlug: PostSlug): Post? {
        val slug = postSlug.value
        val postEntity = postJpaRepo.findBySlug(slug) ?: return null
        return PostEntityMapper.toDomain(postEntity)
    }

    override fun findRecentPosts(offset: Int?): List<Post> {
        val offset = offset ?: 0
        val postEntities = postJpaRepo.findRecentPosts(offset)
        return postEntities.map(PostEntityMapper::toDomain)
    }

    override fun findPopularPosts(offset: Int?): List<Post> {
        val offset = offset ?: 0
        val postEntities = postJpaRepo.findPopularPosts(offset)
        return postEntities.map(PostEntityMapper::toDomain)
    }

    override fun findRecentPostsDetails(offset: Int?): List<PostDetails> {
        val offset = offset ?: 0
        val posts = this.findRecentPosts(offset)
        return posts.map { post ->
            val memberDetails = memberRepo.findMemberDetailsById(post.memberId) ?: return emptyList()
            PostDetailsMapper.toDomain(post, memberDetails)
        }
    }

    override fun findPopularPostsDetails(offset: Int?): List<PostDetails> {
        val offset = offset ?: 0
        val posts = this.findPopularPosts(offset)
        return posts.map { post ->
            val memberDetails = memberRepo.findMemberDetailsById(post.memberId) ?: return emptyList()
            PostDetailsMapper.toDomain(post, memberDetails)
        }
    }

    override fun findPostDetailsBySlug(postSlug: PostSlug): PostDetails? {
        val post = this.findPostBySlug(postSlug) ?: return null
        val memberDetails = memberRepo.findMemberDetailsById(post.memberId) ?: return null
        return PostDetailsMapper.toDomain(post, memberDetails)
    }

    override fun exists(postId: PostId): Boolean =
        postJpaRepo.existsById(postId.stringValue)

    override fun save(post: Post): Post {
        val entity = PostEntityMapper.toEntity(post)
        post.votes?.let { postVotesRepo.saveBulk(it) }
        return PostEntityMapper.toDomain(postJpaRepo.save(entity))
    }

    override fun delete(postId: PostId) =
        postJpaRepo.deleteById(postId.stringValue)
}
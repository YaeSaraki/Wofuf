package dev.saraki.wofuf.modules.forum.infra.repos.impl

import dev.saraki.wofuf.modules.forum.domain.Comment
import dev.saraki.wofuf.modules.forum.domain.valueObjects.CommentDetails
import dev.saraki.wofuf.modules.forum.domain.valueObjects.CommentId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostSlug
import dev.saraki.wofuf.modules.forum.infra.repos.CommentRepo
import dev.saraki.wofuf.modules.forum.infra.repos.jpa.CommentJpaRepo
import dev.saraki.wofuf.modules.forum.infra.repos.jpa.mappers.CommentEntityMapper
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class CommentRepoImpl(
    private val commentJpaRepo: CommentJpaRepo,
) : CommentRepo {

    override fun exists(commentId: CommentId): Boolean =
        commentJpaRepo.existsById(commentId.stringValue)

    override fun findCommentByCommentId(commentId: CommentId): Comment? =
        commentJpaRepo.findById(commentId.stringValue)
            .map(CommentEntityMapper::toDomain)
            .orElse(null)

    override fun findCommentsByPostSlug(postSlug: PostSlug, includeHidden: Boolean): List<Comment> {
        // 管理员/有权限用户：返回所有评论；普通用户：只返回未隐藏的评论
        val comments = if (includeHidden) {
            commentJpaRepo.findByPostEntity_Slug(postSlug.value)
        } else {
            commentJpaRepo.findByPostEntity_SlugAndIsHiddenFalse(postSlug.value)
        }
        return comments.map(CommentEntityMapper::toDomain)
    }

    override fun findCommentDetailsByCommentId(commentId: CommentId): CommentDetails? {
        val entity = commentJpaRepo.findById(commentId.stringValue).orElse(null)
        val memberEntity = entity.memberEntity ?: return null
        return CommentEntityMapper.toCommentDetails(entity)
    }

    @Transactional
    override fun save(comment: Comment): Comment {
        val entity = CommentEntityMapper.toEntity(comment)
        // 不再保存 votes，因为 votes 现在由独立的 CommentVote 聚合管理
        val savedEntity = commentJpaRepo.saveAndFlush(entity)
        return CommentEntityMapper.toDomain(savedEntity)
    }

    @Transactional
    override fun saveBulk(comments: List<Comment>) {
        val entities = comments.map(CommentEntityMapper::toEntity)
        // 批量保存评论（不再保存 votes）
        commentJpaRepo.saveAll(entities)
    }

    @Transactional
    override fun deleteComment(commentId: CommentId) {
        commentJpaRepo.deleteById(commentId.stringValue)
    }

    // ==================== 管理功能方法实现 ====================

    override fun findHiddenComments(page: Int, size: Int): List<Comment> =
        commentJpaRepo.findByIsHiddenTrue(PageRequest.of(page, size))
            .map(CommentEntityMapper::toDomain)

    override fun countHiddenComments(): Long =
        commentJpaRepo.countByIsHiddenTrue()

    override fun countAll(): Long =
        commentJpaRepo.count()

    override fun findAllComments(page: Int, size: Int): List<Comment> =
        commentJpaRepo.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size))
            .map(CommentEntityMapper::toDomain)

    override fun countAllComments(): Long =
        commentJpaRepo.count()
}

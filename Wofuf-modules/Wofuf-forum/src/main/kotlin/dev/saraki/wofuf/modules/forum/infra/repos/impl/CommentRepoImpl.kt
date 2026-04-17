package dev.saraki.wofuf.modules.forum.infra.repos.impl

import dev.saraki.wofuf.modules.forum.domain.Comment
import dev.saraki.wofuf.modules.forum.domain.valueObjects.CommentDetails
import dev.saraki.wofuf.modules.forum.domain.valueObjects.CommentId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostSlug
import dev.saraki.wofuf.modules.forum.infra.repos.CommentRepo
import dev.saraki.wofuf.modules.forum.infra.repos.jpa.CommentJpaRepo
import dev.saraki.wofuf.modules.forum.infra.repos.jpa.mappers.CommentEntityMapper
import dev.saraki.wofuf.shared.domain.UniqueEntityId
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
        // 使用数据库查询过滤，而不是内存过滤
        val entities = if (includeHidden) {
            commentJpaRepo.findByPostEntity_Slug(postSlug.value)
        } else {
            commentJpaRepo.findByPostEntity_SlugAndIsHiddenFalse(postSlug.value)
        }
        return entities.map(CommentEntityMapper::toDomain)
    }

    override fun findRootCommentsByPostSlug(postSlug: PostSlug, includeHidden: Boolean): List<Comment> {
        val entities = if (includeHidden) {
            commentJpaRepo.findByPostEntity_SlugAndParentCommentIdIsNull(postSlug.value)
        } else {
            commentJpaRepo.findByPostEntity_SlugAndParentCommentIdIsNullAndIsHiddenFalse(postSlug.value)
        }
        return entities.map(CommentEntityMapper::toDomain)
    }

    override fun findCommentDetailsByCommentId(commentId: CommentId): CommentDetails? {
        val entity = commentJpaRepo.findById(commentId.stringValue).orElse(null)
        val memberEntity = entity.memberEntity ?: return null
        return CommentEntityMapper.toCommentDetails(entity)
    }

    override fun findCommentDetailsByCommentIds(commentIds: List<CommentId>): Map<CommentId, CommentDetails> {
        if (commentIds.isEmpty()) return emptyMap()

        val entities = commentJpaRepo.findByCommentIdIn(commentIds.map { it.stringValue })
        return entities.associate { entity ->
            val commentId = CommentId.create(UniqueEntityId(entity.commentId)).getOrThrow()
            val details = CommentEntityMapper.toCommentDetails(entity)
            commentId to details
        }
    }

    override fun findChildCommentsByRootId(rootCommentId: CommentId, includeHidden: Boolean): List<Comment> {
        val entities = if (includeHidden) {
            commentJpaRepo.findByRootCommentId(rootCommentId.stringValue)
        } else {
            commentJpaRepo.findByRootCommentIdAndIsHiddenFalse(rootCommentId.stringValue)
        }
        return entities.map(CommentEntityMapper::toDomain)
    }

    @Transactional
    override fun save(comment: Comment): Comment {
        val existingEntity = commentJpaRepo.findById(comment.commentId.stringValue).orElse(null)

        if (existingEntity != null) {
            // 更新现有实体的可变字段，保留 replies 关系
            existingEntity.isHidden = comment.isHidden
            existingEntity.hiddenAt = comment.hiddenAt
            existingEntity.hiddenBy = comment.hiddenBy?.stringValue
            return CommentEntityMapper.toDomain(commentJpaRepo.save(existingEntity))
        }

        // 新评论使用完整构建
        val entity = CommentEntityMapper.toEntity(comment)
        return CommentEntityMapper.toDomain(commentJpaRepo.save(entity))
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

    override fun findCommentsByContentSearch(contentSearch: String, page: Int, size: Int, includeHidden: Boolean): List<Comment> {
        val pageable = PageRequest.of(page, size)
        val entities = if (includeHidden) {
            commentJpaRepo.findByTextContainingIgnoreCase(contentSearch, pageable)
        } else {
            commentJpaRepo.findByTextContainingIgnoreCaseAndIsHidden(contentSearch, false, pageable)
        }
        return entities.map(CommentEntityMapper::toDomain)
    }

    override fun countCommentsByContentSearch(contentSearch: String, includeHidden: Boolean): Long {
        return if (includeHidden) {
            commentJpaRepo.countByTextContainingIgnoreCase(contentSearch)
        } else {
            commentJpaRepo.countByTextContainingIgnoreCaseAndIsHidden(contentSearch, false)
        }
    }

    // ==================== 成员资料页方法实现 ====================

    override fun findCommentsByMemberId(memberId: MemberId, page: Int, size: Int): List<Comment> =
        commentJpaRepo.findByMemberEntity_MemberIdOrderByCreatedAtDesc(memberId.stringValue, PageRequest.of(page, size))
            .map(CommentEntityMapper::toDomain)

    override fun countCommentsByMemberId(memberId: MemberId): Long =
        commentJpaRepo.countByMemberEntity_MemberId(memberId.stringValue)

    @Transactional
    override fun deleteByIdAndMemberId(commentId: CommentId, memberId: MemberId): Boolean {
        val count = commentJpaRepo.deleteByCommentIdAndMemberEntity_MemberId(commentId.stringValue, memberId.stringValue)
        return count > 0
    }
}

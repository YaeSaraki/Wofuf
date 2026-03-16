package dev.saraki.wofuf.modules.forum.infra.repos.impl

import dev.saraki.wofuf.modules.forum.domain.*
import dev.saraki.wofuf.modules.forum.domain.valueObjects.CommentDetails
import dev.saraki.wofuf.modules.forum.domain.valueObjects.CommentId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostSlug
import dev.saraki.wofuf.modules.forum.infra.repos.CommentRepo
import dev.saraki.wofuf.modules.forum.infra.repos.CommentVotesRepo
import dev.saraki.wofuf.modules.forum.infra.repos.jpa.CommentJpaRepo
import dev.saraki.wofuf.modules.forum.infra.repos.jpa.mappers.CommentEntityMapper
import dev.saraki.wofuf.modules.players.domain.valueObjects.PlayerSkin
import dev.saraki.wofuf.modules.players.infra.repos.jpa.PlayerJpaRepo
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/9 15:30
 *   @description: CommentRepo接口的实现类
 */
@Repository
class CommentRepoImpl(
    private val commentJpaRepo: CommentJpaRepo,
    private val commentVotesRepo: CommentVotesRepo,
    private val playerJpaRepo: PlayerJpaRepo,
) : CommentRepo {

    override fun exists(commentId: CommentId): Boolean =
        commentJpaRepo.existsById(commentId.stringValue)

    override fun findCommentByCommentId(commentId: CommentId): Comment? {
        return commentJpaRepo.findById(commentId.stringValue)
            .map(CommentEntityMapper::toDomain)
            .orElse(null)
    }

    override fun findCommentsByPostSlug(postSlug: PostSlug): List<Comment> {
        val slug = postSlug.value
        val commentEntities = commentJpaRepo.findByPostEntity_Slug(slug)
        return commentEntities.map(CommentEntityMapper::toDomain)
    }

    override fun findCommentDetailsByCommentId(commentId: CommentId): CommentDetails? {
        return commentJpaRepo.findById(commentId.stringValue)
            .map { commentEntity ->
                val playerSkin = getPlayerSkinFromMember(commentEntity.memberEntity?.playerId)
                CommentEntityMapper.toCommentDetails(commentEntity, playerSkin)
            }
            .orElse(null)
    }

    private fun getPlayerSkinFromMember(playerId: String?): PlayerSkin? {
        if (playerId == null) return null
        val playerEntity = playerJpaRepo.findById(playerId).orElse(null) ?: return null
        val skinEntity = playerEntity.playerSkin ?: return null
        return PlayerSkin.create(
            type = skinEntity.type ?: "",
            skin = skinEntity.skin,
            cape = skinEntity.cape ?: ""
        ).getOrNull()
    }

    @Transactional
    override fun save(comment: Comment): Comment {
        val entity = CommentEntityMapper.toEntity(comment)

        // 保存评论（不保存 votes，因为新评论没有 votes）
        val savedEntity = commentJpaRepo.saveAndFlush(entity)
        return CommentEntityMapper.toDomain(savedEntity)
    }

    @Transactional
    override fun saveBulk(comments: List<Comment>) {
        val entities = comments.map { comment ->
            val entity = CommentEntityMapper.toEntity(comment)
            entity
        }

        // 批量保存评论
        commentJpaRepo.saveAll(entities)

        // 批量保存投票信息
        comments.forEach { comment ->
            commentVotesRepo.saveBulk(comment.getVotes())
        }
    }

    @Transactional
    override fun deleteComment(commentId: CommentId) {
        commentJpaRepo.deleteById(commentId.stringValue)
    }
}
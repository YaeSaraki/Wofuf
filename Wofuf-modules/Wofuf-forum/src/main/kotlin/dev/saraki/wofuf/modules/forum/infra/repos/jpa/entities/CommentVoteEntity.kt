package dev.saraki.wofuf.modules.forum.infra.repos.jpa.entities

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.DynamicUpdate
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/8 14:00
 *   @description:
 */

@jakarta.persistence.Entity
@DynamicUpdate
@Table(name = "comment_vote")
data class CommentVoteEntity(
    @Id
    @Column(name = "vote_id", nullable = false)
    val voteId: String,

    @Column(name = "comment_id", nullable = false)
    val commentId: String,

    @Column(name = "member_id", nullable = false)
    val memberId: String,

    @Column(name = "vote_type", nullable = false)
    val voteType: String,

    // 与MemberEntity的多对一关系
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "member_id",
        referencedColumnName = "member_id",
        insertable = false, updatable = false,
        foreignKey = ForeignKey(name = "FK_commentVote_member")
    )
    val memberEntity: MemberEntity? = null,

    // 与CommentEntity的多对一关系
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "comment_id",
        referencedColumnName = "comment_id",
        insertable = false, updatable = false,
        foreignKey = ForeignKey(name = "FK_commentVote_comment")
    )
    val commentEntity: CommentEntity? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null
)
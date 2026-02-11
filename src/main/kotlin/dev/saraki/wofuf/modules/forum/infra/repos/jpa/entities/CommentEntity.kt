package dev.saraki.wofuf.modules.forum.infra.repos.jpa.entities

import jakarta.persistence.*
import org.hibernate.annotations.DynamicUpdate
import java.time.LocalDateTime

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/8 14:00
 *   @description:
 */

@Entity
@Table(name = "comment")
@DynamicUpdate
data class CommentEntity(
    @Id
    @Column(name = "comment_id", nullable = false)
    val commentId: String,

    @Column(name = "member_id", nullable = false)
    val memberId: String,

    @Column(name = "post_id", nullable = false)
    val postId: String,

    @Column(name = "parent_comment_id", nullable = false)
    val parentCommentId: String?,

    @Column(name = "text", nullable = false)
    val text: String,

    @Column(name = "points", nullable = true)
    val points: Int?,

    // 与MemberEntity的多对一关系
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "member_id",
        referencedColumnName = "member_id",
        insertable = false, updatable = false,
        foreignKey = ForeignKey(name = "FK_comment_member")
    )
    val memberEntity: MemberEntity? = null,

    // 与PostEntity的多对一关系
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "post_id",
        referencedColumnName = "post_id",
        insertable = false, updatable = false,
        foreignKey = ForeignKey(name = "FK_comment_post")
    )
    val postEntity: PostEntity? = null,

    // 与父评论的多对一关系
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "parent_comment_id",
        referencedColumnName = "parent_comment_id",
        insertable = false, updatable = false,
        foreignKey = ForeignKey(name = "FK_comment_comment_parent")
    )
    val parentComment: CommentEntity? = null,

    // 与自身的一对多关系（用于回复）
    @OneToMany(mappedBy = "parentComment", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val replies: List<CommentEntity> = emptyList(),

    // 与评论点赞的一对多关系
    @OneToMany(mappedBy = "commentEntity", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val commentVoteEntities: List<CommentVoteEntity> = emptyList(),

    @Column(name = "created_at", nullable = true, insertable = false, updatable = false)
    val createdAt: LocalDateTime? = null,

    @Column(name = "updated_at", nullable = true, insertable = false, updatable = false)
    val updatedAt: LocalDateTime? = null,
)
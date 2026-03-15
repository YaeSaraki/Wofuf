package dev.saraki.wofuf.modules.forum.infra.repos.jpa.entities

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.DynamicUpdate
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@jakarta.persistence.Entity
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

    @Column(name = "parent_comment_id", nullable = true)
    val parentCommentId: String? = null,

    @Column(name = "text", nullable = false)
    val text: String,

    @Column(name = "points", nullable = true)
    val points: Int = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "member_id",
        referencedColumnName = "member_id",
        insertable = false,
        updatable = false,
        foreignKey = ForeignKey(name = "FK_comment_member")
    )
    val memberEntity: MemberEntity? = null,

    // 与PostEntity的多对一关系
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "post_id",
        referencedColumnName = "post_id",
        insertable = false,
        updatable = false,
        foreignKey = ForeignKey(name = "FK_comment_post")
    )
    val postEntity: PostEntity? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "parent_comment_id",
        referencedColumnName = "comment_id",
        insertable = false,
        updatable = false,
        foreignKey = ForeignKey(name = "FK_comment_parent"),
        nullable = true
    )
    val parentComment: CommentEntity? = null,

    @OneToMany(
        mappedBy = "parentComment",
        cascade = [CascadeType.ALL],
        fetch = FetchType.LAZY,
        orphanRemoval = true
    )
    val replies: MutableList<CommentEntity> = mutableListOf(),

    // 评论点赞关联
    @OneToMany(
        mappedBy = "commentEntity",
        cascade = [CascadeType.ALL],
        fetch = FetchType.LAZY
    )
    val commentVoteEntities: MutableList<CommentVoteEntity> = mutableListOf(),

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
package dev.saraki.wofuf.modules.forum.infra.repos.jpa.entities

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.DynamicUpdate
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@jakarta.persistence.Entity
@Table(
    name = "comment",
    indexes = [
        Index(name = "idx_comment_post_id", columnList = "post_id"),
        Index(name = "idx_comment_post_hidden", columnList = "post_id, is_hidden"),
        Index(name = "idx_comment_parent_id", columnList = "parent_comment_id"),
        Index(name = "idx_comment_member_id", columnList = "member_id"),
        Index(name = "idx_comment_root_id", columnList = "root_comment_id"),
        Index(name = "idx_comment_short_id", columnList = "short_id", unique = true)
    ]
)
@DynamicUpdate
data class CommentEntity(
    @Id
    @Column(name = "comment_id", nullable = false)
    val commentId: String,

    // 短 ID（用于显示和引用）
    @Column(name = "short_id", nullable = true, unique = true, length = 16)
    val shortId: String? = null,

    @Column(name = "member_id", nullable = false)
    val memberId: String,

    @Column(name = "post_id", nullable = false)
    val postId: String,

    @Column(name = "parent_comment_id", nullable = true)
    val parentCommentId: String? = null,

    // 所属主评论ID（用于Bilibili风格评论：所有子评论扁平化）
    // 主评论的 rootCommentId = null
    // 子评论的 rootCommentId = 所属主评论的ID
    @Column(name = "root_comment_id", nullable = true)
    val rootCommentId: String? = null,

    @Column(name = "text", nullable = false)
    val text: String,

    @Column(name = "points", nullable = true)
    val points: Int = 0,

    // 管理功能相关字段（使用 var 以便 JPA 可以更新）
    @Column(name = "is_hidden", nullable = false)
    var isHidden: Boolean = false,

    @Column(name = "hidden_at", nullable = true)
    var hiddenAt: LocalDateTime? = null,

    @Column(name = "hidden_by", nullable = true)
    var hiddenBy: String? = null,

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

    // 所属主评论（用于 Bilibili 风格评论）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "root_comment_id",
        referencedColumnName = "comment_id",
        insertable = false,
        updatable = false,
        foreignKey = ForeignKey(name = "FK_comment_root"),
        nullable = true
    )
    val rootComment: CommentEntity? = null,

    @OneToMany(
        mappedBy = "parentComment",
        cascade = [CascadeType.ALL],
        fetch = FetchType.LAZY,
        orphanRemoval = true
    )
    val replies: MutableList<CommentEntity> = mutableListOf(),

    // 所有子评论（通过 rootCommentId 关联，用于 Bilibili 风格）
    @OneToMany(
        mappedBy = "rootComment",
        cascade = [],
        fetch = FetchType.LAZY
    )
    val childComments: MutableList<CommentEntity> = mutableListOf(),

    // 评论点赞关联 - 不使用级联，手动管理
    @OneToMany(
        mappedBy = "commentEntity",
        cascade = [],
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
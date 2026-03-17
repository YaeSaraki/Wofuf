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

@Entity
@DynamicUpdate
@Table(
    name = "post_vote",
    uniqueConstraints = [
        UniqueConstraint(
            name = "UK_post_vote_post_member",
            columnNames = ["post_id", "member_id"]
        )
    ]
)
data class PostVoteEntity(
    @Id
    @Column(name = "vote_id", nullable = false)
    val voteId: String,

    @Column(name = "post_id", nullable = false)
    val postId: String,

    @Column(name = "member_id", nullable = false)
    val memberId: String,

    @Column(name = "vote_type", nullable = false)
    val voteType: String,

    // 与MemberEntity的多对一关系
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", referencedColumnName = "member_id", insertable = false, updatable = false)
    val memberEntity: MemberEntity? = null,

    // 与PostEntity的多对一关系
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "post_id",
        referencedColumnName = "post_id",
        insertable = false, updatable = false,
        foreignKey = ForeignKey(name = "FK_postVote_post")
    )
    val postEntity: PostEntity? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null
)
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
@DynamicUpdate
@Table(name = "post_vote")
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

    @Column(name = "created_at", nullable = true, insertable = false, updatable = false)
    val createdAt: LocalDateTime? = null,

    @Column(name = "updated_at", nullable = true, insertable = false, updatable = false)
    val updatedAt: LocalDateTime? = null,
)
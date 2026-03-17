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
@Table(name = "post")
data class PostEntity(
    @Id
    @Column(name = "post_id", nullable = false)
    val postId: String,

    @Column(name = "member_id", nullable = false)
    val memberId: String,

    @Column(name = "slug", nullable = false, unique = true)
    val slug: String,

    @Column(name = "title", nullable = false)
    val title: String,

    @Column(name = "type", nullable = false)
    val type: String,

    @Column(name = "category", nullable = false)
    val category: String = "DISCUSSION",

    @Column(name = "text", nullable = true)
    val text: String?,

    @Column(name = "link", nullable = true)
    val link: String?,

    @Column(name = "total_num_comments", nullable = true)
    val totalNumComments: Int?,

    @Column(name = "points", nullable = false)
    val points: Int,

    @Column(name = "date_time_posted", nullable = false)
    val dateTimePosted: LocalDateTime,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "member_id",
        referencedColumnName = "member_id",
        insertable = false, updatable = false,
        foreignKey = ForeignKey(name = "FK_post_member")
    )
    val memberEntity: MemberEntity? = null,

    @OneToMany(mappedBy = "postEntity", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val comments: List<CommentEntity> = emptyList(),

    @OneToMany(mappedBy = "postEntity", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val postVoteEntities: List<PostVoteEntity> = emptyList(),

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null

    )
 
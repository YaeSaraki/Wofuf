package dev.saraki.wofuf.modules.forum.infra.repos.jpa.entities

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.DynamicUpdate
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/8 14:00
 *   @description: Member领域模型的数据库实体
 */

@Entity
@DynamicUpdate
@Table(name = "member")
data class MemberEntity(
    @Id
    @Column(name = "member_id", nullable = false)
    val memberId: String,

    @Column(name = "player_id", nullable = false, unique = true)
    val playerId: String,

    @Column(name = "user_id", nullable = false, unique = true)
    val userId: String,

    @Column(name = "nickname", nullable = false, unique = true)
    val nickname: String,

    @Column(name = "reputation", nullable = false)
    val reputation: Int,

    // 论坛管理权限字段
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "permissions", nullable = true, columnDefinition = "JSON")
    val permissions: String? = null,

    @Column(name = "is_banned", nullable = false)
    val isBanned: Boolean = false,

    @Column(name = "banned_at", nullable = true)
    val bannedAt: LocalDateTime? = null,

    @Column(name = "banned_until", nullable = true)
    val bannedUntil: LocalDateTime? = null,

    @Column(name = "banned_reason", nullable = true)
    val bannedReason: String? = null,

    @Column(name = "banned_by", nullable = true)
    val bannedBy: String? = null,

    // 与CommentEntity的一对多关系
    @OneToMany(mappedBy = "memberEntity", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val commentEntities: List<CommentEntity> = emptyList(),

    // 与CommentVoteEntity的一对多关系
    @OneToMany(mappedBy = "memberEntity", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val commentVoteEntities: List<CommentVoteEntity> = emptyList(),

    // 与PostEntity的一对多关系
    @OneToMany(mappedBy = "memberEntity", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val postEntities: List<PostEntity> = emptyList(),

    // 与PostVoteEntity的一对多关系
    @OneToMany(mappedBy = "memberEntity", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val postVoteEntities: List<PostVoteEntity> = emptyList(),

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null
)
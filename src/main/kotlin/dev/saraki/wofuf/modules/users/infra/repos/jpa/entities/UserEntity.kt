package dev.saraki.wofuf.modules.users.infra.repos.jpa.entities

import dev.saraki.wofuf.modules.forum.infra.repos.jpa.entities.MemberEntity
import jakarta.persistence.*
import org.hibernate.annotations.DynamicUpdate
import java.time.LocalDateTime

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 16:13
 *   @description:
 */


@Entity
@DynamicUpdate
@Table(name = "users")
data class UserEntity(
    @Id
    @Column(name = "user_id", unique = true, nullable = false)
    val userId: String,

    @Column(name = "email", unique = true, nullable = false)
    val email: String,

    @Column(name = "user_name", unique = true, nullable = false)
    val userName: String,

    @Column(name = "password", nullable = false)
    val password: String,

    @Column(name = "is_email_verified", nullable = false)
    val isEmailVerified: Boolean,

    @Column(name = "is_admin_user", nullable = false)
    val isAdminUser: Boolean,

    @Column(name = "is_deleted", nullable = false)
    val isDeleted: Boolean,

    @Column(name = "last_login", nullable = true)
    val lastLogin: LocalDateTime?,

    @Column(name = "created_at", nullable = true, insertable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = true, insertable = false, updatable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),

    // 与MemberEntity的一对多关系
    @OneToMany(mappedBy = "userEntity", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val memberEntities: List<MemberEntity>? = null,
)
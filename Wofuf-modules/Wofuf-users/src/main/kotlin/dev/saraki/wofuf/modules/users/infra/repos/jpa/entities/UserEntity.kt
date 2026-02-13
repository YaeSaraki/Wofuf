package dev.saraki.wofuf.modules.users.infra.repos.jpa.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.DynamicUpdate
import org.hibernate.annotations.UpdateTimestamp
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

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null
)
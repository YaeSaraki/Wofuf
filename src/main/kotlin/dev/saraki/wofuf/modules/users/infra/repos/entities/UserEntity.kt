package dev.saraki.wofuf.modules.users.infra.repos.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 16:13
 *   @description:
 */


@Entity
@Table(name = "users")
class UserEntity(
    @Id
    @Column(unique = true, nullable = false)
    val id: String,

    @Column(unique = true, nullable = false)
    val email: String,

    @Column(unique = true, nullable = false)
    val username: String,

    @Column(nullable = false)
    val password: String,

    @Column(name = "is_email_verified", nullable = false)
    val isEmailVerified: Boolean = false,

    @Column(name = "is_admin_user", nullable = false)
    val isAdminUser: Boolean = false,

    @Column(name = "is_deleted", nullable = false)
    val isDeleted: Boolean? = false,

    @Column(name = "last_login", nullable = true)
    val lastLogin: LocalDateTime? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),

    ) {
    constructor() : this(
        id = "", // 空字符串占位，后续会被手动指定的 UUID 字符串覆盖
        email = "", // 空字符串占位，后续会被实际邮箱覆盖
        username = "", // 空字符串占位，后续会被实际用户名覆盖
        password = "", // 空字符串占位，后续会被实际加密密码覆盖
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now(),
    )
}
package dev.saraki.wofuf.modules.users.infra.repos.jpa

import dev.saraki.wofuf.modules.users.infra.repos.jpa.entities.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/8 17:12
 *   @description:
 */

interface UserJpaRepo : JpaRepository<UserEntity, String> {

    fun existsByUserName(userName: String): Boolean

    fun findUserByEmail(email: String): UserEntity?

    fun findUserByUserId(id: String): UserEntity?

    fun findUserByUserName(userName: String): UserEntity?

    fun save(user: UserEntity): UserEntity
}
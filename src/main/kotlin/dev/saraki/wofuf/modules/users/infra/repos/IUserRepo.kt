package dev.saraki.wofuf.modules.users.infra.repos

import dev.saraki.wofuf.modules.users.infra.repos.entities.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 16:10
 *   @description:
 */
@Repository
interface IUserRepo: JpaRepository<UserEntity, String> {
    fun existsByUsername(userName: String): Boolean
    fun findByEmail(email: String): Optional<UserEntity>
    fun findUserById(id: String): Optional<UserEntity>
    fun findUserByUsername(username: String): Optional<UserEntity>
    fun save(user: UserEntity): UserEntity
}
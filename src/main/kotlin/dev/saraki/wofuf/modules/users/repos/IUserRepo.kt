package dev.saraki.wofuf.modules.users.repos

import dev.saraki.wofuf.modules.users.domain.User
import dev.saraki.wofuf.modules.users.dtos.UserDto
import dev.saraki.wofuf.modules.users.repos.persistence.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import java.util.Optional

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 16:10
 *   @description:
 */
@Repository
interface IUserRepo: JpaRepository<UserEntity, String> {
    fun findByEmail(email: String): Optional<UserEntity>
    fun save(user: UserEntity): UserEntity
    fun existsByUsername(userName: String): Boolean
    fun getUserById(id: String): Optional<UserEntity>
    fun getUserByUsername(username: String): Optional<UserEntity>
}
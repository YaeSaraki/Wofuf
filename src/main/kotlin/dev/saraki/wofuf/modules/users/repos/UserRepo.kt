package dev.saraki.wofuf.modules.users.repos

import dev.saraki.wofuf.modules.users.domain.User
import java.util.Optional
import java.util.UUID

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 16:10
 *   @description:
 */
interface UserRepo {
    fun findById(id: UUID): Optional<User>
    fun findByUserName(userName: String): Optional<User>
    fun findByEmail(email: String): Optional<User>
    fun save(user: User): User
    fun deleteById(id: UUID)
    fun existsByUserName(userName: String): Boolean
}
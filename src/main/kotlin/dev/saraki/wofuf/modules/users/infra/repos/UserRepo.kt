package dev.saraki.wofuf.modules.users.infra.repos

import dev.saraki.wofuf.modules.users.domain.User
import dev.saraki.wofuf.modules.users.domain.UserEmail
import dev.saraki.wofuf.modules.users.domain.UserId
import dev.saraki.wofuf.modules.users.domain.UserName

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/8 17:09
 *   @description:
 */
interface UserRepo {
    fun existsByUserName(userName: UserName): Boolean
    fun findByUserEmail(userEmail: UserEmail): User?
    fun findUserByUserId(userId: UserId): User?
    fun findUserByUserName(userName: UserName): User?
    fun save(user: User): User
}
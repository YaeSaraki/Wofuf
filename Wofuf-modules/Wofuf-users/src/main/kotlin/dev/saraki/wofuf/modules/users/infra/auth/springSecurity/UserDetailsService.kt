package dev.saraki.wofuf.modules.users.infra.auth.springSecurity

import dev.saraki.wofuf.modules.users.domain.User
import dev.saraki.wofuf.modules.users.domain.valueObjects.UserName
import dev.saraki.wofuf.modules.users.infra.repos.UserRepo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/16 16:47
 *   @description:
 */

@Service
class UserDetailsService: UserDetailsService {

    @Autowired
    private val userRepo: UserRepo? = null

    override fun loadUserByUsername(username: String): User {
        val user = userRepo?.findUserByUserName(UserName.create(username).getOrThrow()) ?: throw Exception("User with username $username not found")
        return user
    }
}
package dev.saraki.wofuf.modules.users.infra.repos.impl

import dev.saraki.wofuf.modules.users.domain.User
import dev.saraki.wofuf.modules.users.domain.UserEmail
import dev.saraki.wofuf.modules.users.domain.UserId
import dev.saraki.wofuf.modules.users.domain.UserName
import dev.saraki.wofuf.modules.users.infra.repos.UserRepo
import dev.saraki.wofuf.modules.users.infra.repos.jpa.UserJpaRepo
import dev.saraki.wofuf.modules.users.infra.repos.jpa.mappers.UserEntityMapper
import org.springframework.stereotype.Repository

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/8 17:13
 *   @description:
 */

@Repository
class UserRepoImpl(
    private val jpaRepository: UserJpaRepo
) : UserRepo {
    override fun existsByUserName(userName: UserName): Boolean {
        return jpaRepository.existsByUserName(userName.value)
    }

    override fun findByUserEmail(userEmail: UserEmail): User? {
        val email = userEmail.value
        return jpaRepository.findUserByEmail(email)?.let(UserEntityMapper::toDomain)
    }

    override fun findUserByUserId(userId: UserId): User? {
        val id = userId.stringValue
        return jpaRepository.findUserByUserId(id)?.let(UserEntityMapper::toDomain)
    }

    override fun findUserByUserName(userName: UserName): User? {
        return jpaRepository.findUserByUserName(userName.value)?.let(UserEntityMapper::toDomain)
    }

    override fun save(user: User): User {
        val entity = UserEntityMapper.toEntity(user)
        return UserEntityMapper.toDomain(jpaRepository.save(entity))
    }
}
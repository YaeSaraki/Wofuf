package dev.saraki.wofuf.modules.forum.infra.repos.jpa

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/9 17:08
 *   @description: '
 */
import dev.saraki.wofuf.modules.forum.infra.repos.jpa.entities.MemberEntity
import org.springframework.data.jpa.repository.JpaRepository

interface MemberJpaRepo : JpaRepository<MemberEntity, String> {
    fun existsByUserId(userId: String): Boolean
    fun findByUserId(userId: String): MemberEntity?
    fun findByNickname(username: String): MemberEntity?
}
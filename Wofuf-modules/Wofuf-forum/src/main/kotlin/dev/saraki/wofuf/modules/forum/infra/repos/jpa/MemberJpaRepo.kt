package dev.saraki.wofuf.modules.forum.infra.repos.jpa

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/9 17:08
 *   @description: '
 */
import dev.saraki.wofuf.modules.forum.infra.repos.jpa.entities.MemberEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface MemberJpaRepo : JpaRepository<MemberEntity, String> {
    fun existsByUserId(userId: String): Boolean
    fun findByUserId(userId: String): MemberEntity?
    fun findByNickname(username: String): MemberEntity?

    // 管理功能方法
    fun findByIsBannedTrue(pageable: Pageable): List<MemberEntity>
    fun countByIsBannedTrue(): Long

    // 活跃成员查询
    fun findByIsBannedFalse(pageable: Pageable): List<MemberEntity>
    fun countByIsBannedFalse(): Long

    // 按昵称搜索（不区分大小写）
    fun findByNicknameContainingIgnoreCase(nickname: String, pageable: Pageable): List<MemberEntity>
    fun countByNicknameContainingIgnoreCase(nickname: String): Long
}
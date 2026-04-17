package dev.saraki.wofuf.modules.forum.infra.repos.impl

import dev.saraki.wofuf.modules.forum.domain.Member
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.NickName
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PermissionPoint
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostLink
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostSlug
import dev.saraki.wofuf.modules.forum.infra.repos.MemberRepo
import dev.saraki.wofuf.modules.forum.infra.repos.jpa.MemberJpaRepo
import dev.saraki.wofuf.modules.forum.infra.repos.jpa.PostJpaRepo
import dev.saraki.wofuf.modules.forum.infra.repos.jpa.mappers.MemberEntityMapper
import dev.saraki.wofuf.modules.users.domain.valueObjects.UserId
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/9 15:30
 *   @description: MemberRepo接口的实现类
 */
@Repository
class MemberRepoImpl(
    private val memberJpaRepo: MemberJpaRepo,
    private val postJpaRepo: PostJpaRepo,
) : MemberRepo {

    override fun exist(userId: UserId): Boolean =
        memberJpaRepo.existsByUserId(userId.stringValue)

    override fun findMemberById(memberId: MemberId): Member? =
        memberJpaRepo.findById(memberId.stringValue)
            .map(MemberEntityMapper::toDomain).orElse(null)

    override fun findMemberByUserId(userId: UserId): Member? =
        memberJpaRepo.findByUserId(userId.stringValue)
            ?.let(MemberEntityMapper::toDomain)

    override fun findMemberIdByUserId(userId: UserId): MemberId? =
        memberJpaRepo.findByUserId(userId.stringValue)
            ?.let { MemberId.create(UniqueEntityId(it.memberId)).getOrThrow() }

    override fun findMemberByPostSlug(postSlug: PostSlug): Member? {
        val postEntity = postJpaRepo.findBySlug(postSlug.value) ?: return null
        val memberEntity = postEntity.memberEntity ?: return null
        return MemberEntityMapper.toDomain(memberEntity)
    }

    override fun findMemberByPostLink(postLink: PostLink): Member? {
        val postEntity = postJpaRepo.findPostEntityByLink(postLink.value) ?: return null
        val memberEntity = postEntity.memberEntity ?: return null
        return MemberEntityMapper.toDomain(memberEntity)
    }

    override fun findMemberByNickName(nickName: NickName): Member? =
        memberJpaRepo.findByNickname(nickName.value)
            ?.let(MemberEntityMapper::toDomain)


    @Transactional
    override fun save(member: Member): Member? {
        val memberEntity = MemberEntityMapper.toEntity(member)
        val savedEntity = memberJpaRepo.save(memberEntity)
        return MemberEntityMapper.toDomain(savedEntity)
    }

    @Transactional
    override fun updateNickname(memberId: MemberId, newNickname: NickName): Boolean {
        val member = findMemberById(memberId) ?: return false
        val updated = member.updateNickname(newNickname).getOrThrow()
        save(updated)
        return true
    }

    // ==================== 管理功能方法实现 ====================

    override fun findBannedMembers(page: Int, size: Int): List<Member> =
        memberJpaRepo.findByIsBannedTrue(PageRequest.of(page, size))
            .map(MemberEntityMapper::toDomain)

    override fun countBannedMembers(): Long =
        memberJpaRepo.countByIsBannedTrue()

    override fun findMembersByPermission(permission: PermissionPoint, page: Int, size: Int): List<Member> {
        // 由于权限存储为JSON，这里使用简单的过滤方式
        return memberJpaRepo.findAll(PageRequest.of(page, size)).content
            .filter { entity ->
                entity.permissions?.contains(permission.name) == true
            }
            .map(MemberEntityMapper::toDomain)
    }

    override fun findActiveMembers(page: Int, size: Int): List<Member> =
        memberJpaRepo.findByIsBannedFalse(PageRequest.of(page, size))
            .map(MemberEntityMapper::toDomain)

    override fun countActiveMembers(): Long =
        memberJpaRepo.countByIsBannedFalse()

    override fun countAllMembers(): Long =
        memberJpaRepo.count()

    override fun findMembersByNickname(nickname: String, page: Int, size: Int): List<Member> =
        memberJpaRepo.findByNicknameContainingIgnoreCase(nickname, PageRequest.of(page, size))
            .map(MemberEntityMapper::toDomain)

    override fun countMembersByNickname(nickname: String): Long =
        memberJpaRepo.countByNicknameContainingIgnoreCase(nickname)
}
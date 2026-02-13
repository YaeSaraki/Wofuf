package dev.saraki.wofuf.modules.forum.infra.repos.impl

import dev.saraki.wofuf.modules.forum.domain.*
import dev.saraki.wofuf.modules.forum.infra.repos.MemberRepo
import dev.saraki.wofuf.modules.forum.infra.repos.jpa.MemberJpaRepo
import dev.saraki.wofuf.modules.forum.infra.repos.jpa.PostJpaRepo
import dev.saraki.wofuf.modules.forum.infra.repos.jpa.mappers.MemberEntityMapper
import dev.saraki.wofuf.modules.users.domain.valueObjects.UserId
import dev.saraki.wofuf.modules.users.infra.repos.jpa.UserJpaRepo
import dev.saraki.wofuf.shared.domain.UniqueEntityId
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
}
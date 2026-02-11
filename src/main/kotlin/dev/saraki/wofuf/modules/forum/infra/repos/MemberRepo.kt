package dev.saraki.wofuf.modules.forum.infra.repos

import dev.saraki.wofuf.modules.forum.domain.*
import dev.saraki.wofuf.modules.users.domain.UserId

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/9 15:30
 *   @description: Member领域仓储接口
 */
interface MemberRepo {
    fun exist(userId: UserId): Boolean
    fun findMemberById(memberId: MemberId): Member?
    fun findMemberByUserId(userId: UserId): Member?
    fun findMemberIdByUserId(userId: UserId): MemberId?
    fun findMemberByPostSlug(postSlug: PostSlug): Member?
    fun findMemberByPostLink(postLink: PostLink): Member?
    fun findMemberByNickName(nickName: NickName): Member?

    fun findMemberDetailsById(memberId: MemberId): MemberDetails?
    fun findMemberDetailsByUserId(userId: UserId): MemberDetails?
    fun findMemberDetailsByNickName(nickName: NickName): MemberDetails?
    fun findMemberDetailsByPostSlug(postSlug: PostSlug): MemberDetails?
    fun findMemberDetailsByPostLink(postLink: PostLink): MemberDetails?
    fun save(member: Member): Member?
}
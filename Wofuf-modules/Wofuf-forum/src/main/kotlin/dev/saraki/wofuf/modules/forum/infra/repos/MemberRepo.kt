package dev.saraki.wofuf.modules.forum.infra.repos

import dev.saraki.wofuf.modules.forum.domain.Member
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.NickName
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PermissionPoint
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostLink
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostSlug
import dev.saraki.wofuf.modules.users.domain.valueObjects.UserId

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
    fun save(member: Member): Member?

    // ==================== 管理功能方法 ====================

    /**
     * 分页获取封禁用户列表
     */
    fun findBannedMembers(page: Int, size: Int): List<Member>

    /**
     * 统计封禁用户数量
     */
    fun countBannedMembers(): Long

    /**
     * 根据权限查找成员
     */
    fun findMembersByPermission(permission: PermissionPoint, page: Int, size: Int): List<Member>

    /**
     * 分页获取活跃成员列表（未被封禁）
     */
    fun findActiveMembers(page: Int, size: Int): List<Member>

    /**
     * 统计活跃成员数量
     */
    fun countActiveMembers(): Long

    /**
     * 统计所有成员数量（包含封禁的）
     */
    fun countAllMembers(): Long

    /**
     * 按昵称搜索成员（不区分大小写）
     */
    fun findMembersByNickname(nickname: String, page: Int, size: Int): List<Member>

    /**
     * 统计昵称搜索结果数量
     */
    fun countMembersByNickname(nickname: String): Long
}
package dev.saraki.wofuf.modules.forum.domain

import dev.saraki.wofuf.modules.forum.domain.events.MemberCreated
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.NickName
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PermissionPoint
import dev.saraki.wofuf.modules.players.domain.valueObjects.PlayerId
import dev.saraki.wofuf.modules.users.domain.valueObjects.UserId
import dev.saraki.wofuf.shared.core.AppError
import dev.saraki.wofuf.shared.core.Guard
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.domain.AggregateRoot
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import java.time.LocalDateTime

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/8 15:14
 *   @description:
 */
data class MemberProps(
    val userId: UserId,
    val playerId: PlayerId,
    val nickName: NickName,
    val reputation: Int = 0,
    // 论坛管理权限（细粒度控制）
    val permissions: Set<PermissionPoint> = emptySet(),
    val isBanned: Boolean = false,
    val bannedAt: LocalDateTime? = null,
    val bannedUntil: LocalDateTime? = null,
    val bannedReason: String? = null,
    val bannedBy: MemberId? = null
)

class Member private constructor(
    props: MemberProps,
    id: UniqueEntityId?
) : AggregateRoot<MemberProps>(props, id) {

    val memberId: MemberId
        get() = MemberId.create(_id).getOrThrow()

    val userId: UserId
        get() = props.userId

    val playerId: PlayerId
        get() = props.playerId

    val nickname: NickName
        get() = props.nickName

    val reputation: Int
        get() = props.reputation

    // 论坛管理权限
    val permissions: Set<PermissionPoint>
        get() = props.permissions

    val isBanned: Boolean
        get() = props.isBanned

    val bannedAt: LocalDateTime?
        get() = props.bannedAt

    val bannedUntil: LocalDateTime?
        get() = props.bannedUntil

    val bannedReason: String?
        get() = props.bannedReason

    val bannedBy: MemberId?
        get() = props.bannedBy

    // ==================== 管理功能方法 ====================

    /**
     * 检查是否拥有指定论坛权限
     */
    fun hasPermission(permission: PermissionPoint): Boolean {
        return props.permissions.contains(permission)
    }

    /**
     * 授予权限
     */
    fun grantPermission(permission: PermissionPoint): Result<Member> {
        if (props.permissions.contains(permission)) {
            return Result.failure(AppError("已拥有该权限"))
        }
        val newPermissions = props.permissions + permission
        val newProps = props.copy(permissions = newPermissions)
        return Member.create(newProps, _id)
    }

    /**
     * 撤销权限
     */
    fun revokePermission(permission: PermissionPoint): Result<Member> {
        if (!props.permissions.contains(permission)) {
            return Result.failure(AppError("未拥有该权限"))
        }
        val newPermissions = props.permissions - permission
        val newProps = props.copy(permissions = newPermissions)
        return Member.create(newProps, _id)
    }

    /**
     * 封禁用户
     */
    fun ban(until: LocalDateTime, reason: String, by: MemberId): Result<Member> {
        if (props.isBanned) {
            return Result.failure(AppError("用户已被封禁"))
        }
        val newProps = props.copy(
            isBanned = true,
            bannedAt = LocalDateTime.now(),
            bannedUntil = until,
            bannedReason = reason,
            bannedBy = by
        )
        return Member.create(newProps, _id)
    }

    /**
     * 解封用户
     */
    fun unban(): Result<Member> {
        if (!props.isBanned) {
            return Result.failure(AppError("用户未被封禁"))
        }
        val newProps = props.copy(
            isBanned = false,
            bannedAt = null,
            bannedUntil = null,
            bannedReason = null,
            bannedBy = null
        )
        return Member.create(newProps, _id)
    }

    /**
     * 检查封禁是否已过期
     */
    fun isBanExpired(): Boolean {
        if (!props.isBanned) return true
        val until = props.bannedUntil ?: return false
        return LocalDateTime.now().isAfter(until)
    }

    companion object {
        fun create(props: MemberProps, id: UniqueEntityId?): Result<Member> {
            val guardResult = Guard.againstNullOrUndefinedBulk(
                listOf(
                    Guard.GuardArgument(props.userId, "userId"),
                    Guard.GuardArgument(props.playerId, "playerId"),
                    Guard.GuardArgument(props.nickName, "nickname")
                )
            )
            if (guardResult.isFailure) {
                return Result.failure(guardResult.exceptionOrThrow())
            }

            val defaultProps = MemberProps(
                userId = props.userId,
                playerId = props.playerId,
                nickName = props.nickName,
                reputation = props.reputation,
                permissions = props.permissions,
                isBanned = props.isBanned,
                bannedAt = props.bannedAt,
                bannedUntil = props.bannedUntil,
                bannedReason = props.bannedReason,
                bannedBy = props.bannedBy
            )

            // 判断是否为新用户：id为空则为新创建
            val isNewMember = id == null

            val member = Member(defaultProps, id)

            if (isNewMember) {
                member.addDomainEvent(MemberCreated(member))
            }

            // 校验成功，返回成功Result
            return Result.success(member)
        }
    }
}
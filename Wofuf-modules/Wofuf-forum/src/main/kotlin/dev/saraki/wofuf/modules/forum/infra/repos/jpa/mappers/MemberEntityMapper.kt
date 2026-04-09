package dev.saraki.wofuf.modules.forum.infra.repos.jpa.mappers

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dev.saraki.wofuf.modules.forum.domain.Member
import dev.saraki.wofuf.modules.forum.domain.MemberProps
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberDetails
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberDetailsProps
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.NickName
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PermissionPoint
import dev.saraki.wofuf.modules.forum.infra.repos.jpa.entities.MemberEntity
import dev.saraki.wofuf.modules.players.domain.valueObjects.PlayerId
import dev.saraki.wofuf.modules.users.domain.valueObjects.UserId
import dev.saraki.wofuf.shared.domain.UniqueEntityId

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/8 22:48
 *   @description:
 */
object MemberEntityMapper {

    private val gson = Gson()

    private fun parsePermissions(json: String?): Set<PermissionPoint> {
        if (json.isNullOrEmpty()) return emptySet()
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            val list: List<String> = gson.fromJson(json, type)
            list.mapNotNull { 
                try { PermissionPoint.valueOf(it) } catch (e: Exception) { null }
            }.toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    private fun serializePermissions(permissions: Set<PermissionPoint>): String? {
        if (permissions.isEmpty()) return null
        return gson.toJson(permissions.map { it.name })
    }

    fun toDomain(memberEntity: MemberEntity): Member {
        val member = Member.create(
            props = MemberProps(
                userId = UserId.create(UniqueEntityId(memberEntity.userId)).getOrThrow(),
                playerId = PlayerId.create(UniqueEntityId(memberEntity.playerId)).getOrThrow(),
                nickName = NickName.create(memberEntity.nickname).getOrThrow(),
                reputation = memberEntity.reputation,
                // 论坛权限
                permissions = parsePermissions(memberEntity.permissions),
                isBanned = memberEntity.isBanned,
                bannedAt = memberEntity.bannedAt,
                bannedUntil = memberEntity.bannedUntil,
                bannedReason = memberEntity.bannedReason,
                bannedBy = memberEntity.bannedBy?.let { 
                    MemberId.create(UniqueEntityId(it)).getOrThrow() 
                }
            ),
            UniqueEntityId(memberEntity.memberId)
        ).getOrThrow()

        member._createdAt = memberEntity.createdAt
        member._updatedAt = memberEntity.updatedAt

        return member
    }

    fun toMemberDetails(memberEntity: MemberEntity): MemberDetails {
        return MemberDetails.create(
            MemberDetailsProps(
                nickName = NickName.create(memberEntity.nickname).getOrThrow(),
                reputation = memberEntity.reputation,
                playerId = memberEntity.playerId?.let { 
                    PlayerId.create(UniqueEntityId(it)).getOrThrow() 
                },
            )
        ).getOrThrow()
    }

    fun toEntity(domain: Member): MemberEntity {
        return MemberEntity(
            userId = domain.userId.stringValue,
            playerId = domain.playerId.stringValue,
            memberId = domain.memberId.stringValue,
            nickname = domain.nickname.value,
            reputation = domain.reputation,
            // 论坛权限
            permissions = serializePermissions(domain.permissions),
            isBanned = domain.isBanned,
            bannedAt = domain.bannedAt,
            bannedUntil = domain.bannedUntil,
            bannedReason = domain.bannedReason,
            bannedBy = domain.bannedBy?.stringValue
        )
    }
}
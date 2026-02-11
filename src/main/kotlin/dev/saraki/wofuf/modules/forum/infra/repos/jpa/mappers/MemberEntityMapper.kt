package dev.saraki.wofuf.modules.forum.infra.repos.jpa.mappers

import dev.saraki.wofuf.modules.forum.domain.Member
import dev.saraki.wofuf.modules.forum.domain.MemberProps
import dev.saraki.wofuf.modules.forum.domain.NickName
import dev.saraki.wofuf.modules.forum.infra.repos.jpa.entities.MemberEntity
import dev.saraki.wofuf.modules.players.domain.PlayerId
import dev.saraki.wofuf.modules.users.domain.UserId
import dev.saraki.wofuf.shared.domain.UniqueEntityId

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/8 22:48
 *   @description:
 */
object MemberEntityMapper {

    fun toDomain(memberEntity: MemberEntity): Member {

        val playerEntity = memberEntity.playerEntity!!

        val member = Member.create(
            props = MemberProps(
                userId = UserId.create(UniqueEntityId(memberEntity.userId)).getOrThrow(),
                playerId = PlayerId.create(UniqueEntityId(playerEntity.playerId)).getOrThrow(),
                nickName = NickName.create(memberEntity.nickname).getOrThrow(),
                reputation = memberEntity.reputation
            )
        ).getOrThrow()

        member._createdAt = memberEntity.createdAt
        member._updatedAt = memberEntity.updatedAt

        return member
    }

    fun toEntity(domain: Member): MemberEntity {
        return MemberEntity(
            userId = domain.userId.stringValue,
            playerId = domain.playerId.stringValue,
            memberId = domain.memberId.stringValue,
            nickname = domain.nickname.value,
            reputation = domain.reputation
        )
    }
}
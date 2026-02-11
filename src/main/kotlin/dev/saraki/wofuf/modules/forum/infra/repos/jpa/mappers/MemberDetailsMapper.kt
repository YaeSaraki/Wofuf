package dev.saraki.wofuf.modules.forum.infra.repos.jpa.mappers

import dev.saraki.wofuf.modules.forum.domain.MemberDetails
import dev.saraki.wofuf.modules.forum.domain.MemberDetailsProps
import dev.saraki.wofuf.modules.forum.domain.NickName
import dev.saraki.wofuf.modules.forum.infra.repos.jpa.entities.MemberEntity
import dev.saraki.wofuf.modules.players.domain.PlayerName
import dev.saraki.wofuf.modules.users.domain.UserName

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/9 20:07
 *   @description:
 */
object MemberDetailsMapper {
    fun toDomain(memberEntity: MemberEntity): MemberDetails {
        val userEntity = memberEntity.userEntity!!
        val playerEntity = memberEntity.playerEntity!!

        return MemberDetails.create(
            MemberDetailsProps(
                userName = UserName.create(userEntity.userName).getOrThrow(),
                nickName = NickName.create(memberEntity.nickname).getOrThrow(),
                playerName = PlayerName.create(playerEntity.playerName).getOrThrow(),
                reputation = memberEntity.reputation,
                isEmailVerified = userEntity.isEmailVerified,
                isAdminUser = userEntity.isAdminUser,
                isDeleted = userEntity.isDeleted
            )
        ).getOrThrow()
    }
}
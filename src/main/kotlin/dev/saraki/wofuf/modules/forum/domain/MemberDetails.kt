package dev.saraki.wofuf.modules.forum.domain

import dev.saraki.wofuf.modules.players.domain.PlayerName
import dev.saraki.wofuf.modules.users.domain.UserName
import dev.saraki.wofuf.shared.core.Guard
import dev.saraki.wofuf.shared.core.Result

import dev.saraki.wofuf.shared.domain.ValueObject

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/6 22:29
 *   @description:
 */
data class MemberDetailsProps(
    val reputation: Int,
    val nickName: NickName,
    val userName: UserName,
    val playerName: PlayerName,
    val isEmailVerified: Boolean?,
    val isAdminUser: Boolean?,
    val isDeleted: Boolean?
)

class MemberDetails(
    props: MemberDetailsProps,
) : ValueObject<MemberDetailsProps>(props) {

    val reputation: Int
        get() = props.reputation

    val nickName: NickName
        get() = props.nickName

    val userName: UserName
        get() = props.userName

    val playerName: PlayerName
        get() = props.playerName

    val isEmailVerified: Boolean?
        get() = props.isEmailVerified

    val isAdminUser: Boolean?
        get() = props.isAdminUser

    val isDeleted: Boolean?
        get() = props.isDeleted

    companion object {

        fun create(props: MemberDetailsProps): Result<MemberDetails> {
            Guard.againstNullOrUndefinedBulk(
                listOf(
                    Guard.GuardArgument(props.nickName, "name"),
                    Guard.GuardArgument(props.reputation, "reputation")
                )
            )
            return Result.success(MemberDetails(props))
        }

        val UNKNOWN = MemberDetails(
            props = MemberDetailsProps(
                reputation = -1,
                nickName = NickName.UNKNOWN,
                userName = UserName.UNKNOWN,
                playerName = PlayerName.UNKNOWN,
                isEmailVerified = false,
                isAdminUser = false,
                isDeleted = false
            )
        )
    }
}

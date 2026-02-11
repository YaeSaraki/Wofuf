package dev.saraki.wofuf.modules.forum.domain

import dev.saraki.wofuf.modules.players.domain.PlayerId
import dev.saraki.wofuf.modules.users.domain.UserId
import dev.saraki.wofuf.shared.core.Guard
import dev.saraki.wofuf.shared.domain.AggregateRoot
import dev.saraki.wofuf.shared.domain.UniqueEntityId

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

    companion object {
        fun create(props: MemberProps, id: UniqueEntityId? = null): Result<Member> {
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

            id == null

            val defaultProps = MemberProps(
                userId = props.userId,
                playerId = props.playerId,
                nickName = props.nickName,
                reputation = props.reputation
            )

            val member = Member(defaultProps, id)

            // 校验成功，返回成功Result
            return Result.success(member)
        }
    }
}
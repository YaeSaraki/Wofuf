package dev.saraki.wofuf.modules.forum.useCases.members.createMember

import dev.saraki.wofuf.modules.forum.domain.Member
import dev.saraki.wofuf.modules.forum.domain.MemberProps
import dev.saraki.wofuf.modules.forum.domain.valueObjects.NickName
import dev.saraki.wofuf.modules.forum.infra.repos.MemberRepo
import dev.saraki.wofuf.modules.players.domain.valueObjects.PlayerId
import dev.saraki.wofuf.modules.users.domain.valueObjects.UserId
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import dev.saraki.wofuf.shared.utils.HashVerifyUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/11 17:54
 *   @description:
 */
@Service
class CreateMemberUseCase(
    private val memberRepository: MemberRepo,
    @Value("\${secretKey}")
    private val secretKey: String = "This is a secret key that only the server and the client know."
) : UseCase<CreateMemberDto.Request, Unit> {
    override fun execute(request: CreateMemberDto.Request): Result<Unit> {
        // 验证 code 是否正确
        val verify = HashVerifyUtil.verifyCode(request.playerId, request.code, secretKey)
        if (!verify) {
            return CreateMemberErrors.CodeError()
        }

        val userIdOrError = UserId.create(UniqueEntityId(request.userId))
        val playerIdOrError = PlayerId.create(UniqueEntityId(request.playerId))
        val nickNameOrError = NickName.create(request.nickName)

        val result = Result.combine(userIdOrError, playerIdOrError, nickNameOrError)
        if (result.isFailure) {
            return Result.failure(result.exceptionOrThrow())
        }

        val userId = userIdOrError.getOrThrow()
        val playerId = playerIdOrError.getOrThrow()
        val nickName = nickNameOrError.getOrThrow()

        // 创建Member
        val memberResult = Member.create(
            MemberProps(
                userId = userId,
                playerId = playerId,
                nickName = nickName,
                reputation = 0
            )
        )

        if (memberResult.isFailure) {
            return Result.failure(memberResult.exceptionOrThrow())
        }

        val member = memberResult.getOrThrow()

        memberRepository.save(member)

        return Result.success(Unit)
    }
}
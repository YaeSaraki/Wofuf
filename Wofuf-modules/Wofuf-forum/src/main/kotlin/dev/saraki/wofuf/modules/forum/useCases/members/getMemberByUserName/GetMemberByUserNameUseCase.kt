package dev.saraki.wofuf.modules.forum.useCases.members.getMemberByUserName

import dev.saraki.wofuf.modules.forum.domain.valueObjects.NickName
import dev.saraki.wofuf.modules.forum.infra.repos.MemberRepo
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import org.springframework.stereotype.Service

/**
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 * @date 2026/3/15 16:15
 * @description Get member by username (nickname)
 */
@Service
class GetMemberByUserNameUseCase(
    private val memberRepo: MemberRepo,
) : UseCase<GetMemberByUserNameDto.Request, GetMemberByUserNameDto.Response> {
    override fun execute(request: GetMemberByUserNameDto.Request): Result<GetMemberByUserNameDto.Response> {
        if (request.username.isBlank()) {
            return GetMemberByUserNameErrors.UsernameEmptyError()
        }

        // Validate nickname
        val nickNameOrError = NickName.create(request.username)
        if (nickNameOrError.isFailure) {
            return GetMemberByUserNameErrors.MemberNotFoundError(request.username)
        }
        val nickName = nickNameOrError.getOrThrow()

        // Get member
        val member = memberRepo.findMemberByNickName(nickName) ?: return GetMemberByUserNameErrors.MemberNotFoundError(request.username)

        return Result.success(
            GetMemberByUserNameDto.Response(
                memberId = member.memberId.stringValue,
                userId = member.userId.stringValue,
                playerId = member.playerId.stringValue,
                nickname = member.nickname.value,
                reputation = member.reputation,
            )
        )
    }
}

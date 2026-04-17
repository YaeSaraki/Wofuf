package dev.saraki.wofuf.modules.forum.useCases.members.updateNickname

import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.NickName
import dev.saraki.wofuf.modules.forum.infra.repos.MemberRepo
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import org.springframework.stereotype.Service

@Service
class UpdateMemberNicknameUseCase(
    private val memberRepo: MemberRepo,
) : UseCase<UpdateMemberNicknameDto.Request, UpdateMemberNicknameDto.Response> {

    override fun execute(request: UpdateMemberNicknameDto.Request): Result<UpdateMemberNicknameDto.Response> {
        if (request.memberId.isBlank()) {
            return UpdateMemberNicknameErrors.MemberIdEmptyError()
        }

        if (request.newNickname.isBlank()) {
            return UpdateMemberNicknameErrors.NicknameEmptyError()
        }

        // Create value objects
        val memberId = MemberId.create(UniqueEntityId(request.memberId)).getOrThrow()
        val nicknameOrNull = NickName.create(request.newNickname)
        if (nicknameOrNull.isFailure) {
            return UpdateMemberNicknameErrors.InvalidNicknameError()
        }
        val nickname = nicknameOrNull.getOrThrow()

        // Find member
        val member = memberRepo.findMemberById(memberId)
            ?: return UpdateMemberNicknameErrors.MemberNotFoundError()

        // Check nickname uniqueness (exclude current member)
        val existingWithNickname = memberRepo.findMemberByNickName(nickname)
        if (existingWithNickname != null && existingWithNickname.memberId != member.memberId) {
            return UpdateMemberNicknameErrors.NicknameAlreadyTakenError()
        }

        // Update nickname
        val updated = member.updateNickname(nickname).getOrThrow()
        memberRepo.save(updated)

        return Result.success(
            UpdateMemberNicknameDto.Response(
                success = true,
                message = "Nickname updated successfully"
            )
        )
    }
}

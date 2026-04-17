package dev.saraki.wofuf.modules.forum.useCases.members.getMemberById

import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
import dev.saraki.wofuf.modules.forum.infra.repos.MemberRepo
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import org.springframework.stereotype.Service

/**
 * @author YaeSaraki
 * @date 2026/4/16
 * @description Use case for getting a member by ID
 */
@Service
class GetMemberByIdUseCase(
    private val memberRepo: MemberRepo
) : UseCase<GetMemberByIdDto.Request, GetMemberByIdDto.Response> {

    override fun execute(request: GetMemberByIdDto.Request): Result<GetMemberByIdDto.Response> {
        // 1. Validate member ID
        if (request.memberId.isBlank()) {
            return GetMemberByIdErrors.MemberIdEmptyError()
        }

        // 2. Create MemberId from string
        val memberIdOrError = try {
            MemberId.create(UniqueEntityId(request.memberId))
        } catch (e: IllegalArgumentException) {
            return GetMemberByIdErrors.InvalidMemberIdError(request.memberId)
        }
        if (memberIdOrError.isFailure) {
            return GetMemberByIdErrors.InvalidMemberIdError(request.memberId)
        }
        val memberId = memberIdOrError.getOrThrow()

        // 3. Find the member by ID
        val member = memberRepo.findMemberById(memberId)
            ?: return GetMemberByIdErrors.MemberNotFoundError(request.memberId)

        // 4. Return response with nickname
        return Result.success(
            GetMemberByIdDto.Response(
                memberId = request.memberId,
                nickname = member.nickname.value
            )
        )
    }
}
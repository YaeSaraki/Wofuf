package dev.saraki.wofuf.modules.forum.useCases.members.updateNickname

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.modules.forum.domain.valueObjects.NickName
import dev.saraki.wofuf.modules.forum.infra.security.requireCurrentUserId
import dev.saraki.wofuf.modules.forum.infra.repos.MemberRepo
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.web.bind.annotation.*

@RestController
class UpdateMemberNicknameController(
    private val updateNicknameUseCase: UpdateMemberNicknameUseCase,
    private val memberRepo: MemberRepo,
) : BaseController() {

    @PutMapping(ForumApiConstantV1.Members.MEMBER_PROFILE)
    fun updateNickname(
        @PathVariable nickname: String,
        @RequestBody request: UpdateMemberNicknameDto.Request
    ): ApiResponse<UpdateMemberNicknameDto.Response> {
        // Verify ownership - only the profile owner can change their nickname
        val currentUserId = try {
            requireCurrentUserId()
        } catch (e: IllegalStateException) {
            return ApiResponse.error("Unauthorized")
        }

        val member = memberRepo.findMemberByNickName(
            NickName.create(nickname).getOrNull()
                ?: return ApiResponse.error("Invalid nickname format")
        )

        if (member == null) {
            return ApiResponse.error("Member not found")
        }

        if (member.userId.stringValue != currentUserId) {
            return ApiResponse.error("Forbidden: you can only update your own nickname")
        }

        // Pass memberId in request for use case (controller has already verified ownership)
        val useCaseRequest = UpdateMemberNicknameDto.Request(
            memberId = member.memberId.stringValue,
            newNickname = request.newNickname
        )

        val result = updateNicknameUseCase.execute(useCaseRequest)

        return if (result.isSuccess) {
            ApiResponse.success(result.getOrThrow())
        } else {
            ApiResponse.error(result.exceptionOrNull()?.message ?: "Update failed")
        }
    }
}

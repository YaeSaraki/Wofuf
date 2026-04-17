package dev.saraki.wofuf.modules.forum.useCases.members.getMemberProfile

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.modules.forum.domain.valueObjects.NickName
import dev.saraki.wofuf.modules.forum.infra.repos.MemberRepo
import dev.saraki.wofuf.modules.forum.useCases.admin.members.getMemberProfile.GetMemberProfileDto
import dev.saraki.wofuf.modules.forum.useCases.admin.members.getMemberProfile.GetMemberProfileUseCase
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.web.bind.annotation.*

@RestController("publicGetMemberProfileController")
@RequestMapping(ForumApiConstantV1.Members.MEMBER_PROFILE)
class GetMemberProfileController(
    private val getMemberProfileUseCase: GetMemberProfileUseCase,
    private val memberRepo: MemberRepo
) : BaseController() {

    @GetMapping
    fun getMemberProfile(
        @PathVariable nickname: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ApiResponse<GetMemberProfileDto.Response> {
        // First resolve nickname to memberId
        val nicknameObj = NickName.create(nickname).getOrNull()
            ?: return ApiResponse.error("Invalid nickname format")

        val member = memberRepo.findMemberByNickName(nicknameObj)
            ?: return ApiResponse.error("Member not found")

        // Now call use case with memberId
        val result = getMemberProfileUseCase.execute(
            GetMemberProfileDto.Request(
                memberId = member.memberId.stringValue,
                page = page,
                size = size
            )
        )

        return if (result.isSuccess) {
            ApiResponse.success(result.getOrThrow())
        } else {
            ApiResponse.error(result.exceptionOrNull()?.message ?: "Failed to get member profile")
        }
    }
}

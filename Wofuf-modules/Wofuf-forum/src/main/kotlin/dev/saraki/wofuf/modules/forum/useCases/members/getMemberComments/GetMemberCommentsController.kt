package dev.saraki.wofuf.modules.forum.useCases.members.getMemberComments

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.modules.forum.domain.valueObjects.NickName
import dev.saraki.wofuf.modules.forum.infra.repos.MemberRepo
import dev.saraki.wofuf.modules.forum.useCases.members.getMemberComments.GetMemberCommentsDto.Request
import dev.saraki.wofuf.modules.forum.useCases.members.getMemberComments.GetMemberCommentsDto.Response
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.web.bind.annotation.*

@RestController("publicGetMemberCommentsController")
@RequestMapping(ForumApiConstantV1.Members.MEMBER_COMMENTS)
class GetMemberCommentsController(
    private val getMemberCommentsUseCase: GetMemberCommentsUseCase,
    private val memberRepo: MemberRepo
) : BaseController() {

    @GetMapping
    fun getMemberComments(
        @PathVariable nickname: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ApiResponse<Response> {
        val nicknameObj = NickName.create(nickname).getOrNull()
            ?: return ApiResponse.error("Invalid nickname format")

        val member = memberRepo.findMemberByNickName(nicknameObj)
            ?: return ApiResponse.error("Member not found")

        val result = getMemberCommentsUseCase.execute(
            Request(
                memberId = member.memberId.stringValue,
                page = page,
                size = size
            )
        )

        return if (result.isSuccess) {
            ApiResponse.success(result.getOrThrow())
        } else {
            ApiResponse.error(result.exceptionOrNull()?.message ?: "Failed to get member comments")
        }
    }
}

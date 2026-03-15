package dev.saraki.wofuf.modules.forum.useCases.members.getMemberByUserName

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.web.bind.annotation.*

/**
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 * @date 2026/3/15 16:15
 * @description Get member by username (nickname)
 */
@RestController
@RequestMapping(ForumApiConstantV1.Members.BY_USERNAME)
class GetMemberByUserNameController(
    private val getMemberByUserNameUseCase: GetMemberByUserNameUseCase
) : BaseController() {

    @GetMapping
    fun getMemberByUserName(@PathVariable username: String): ApiResponse<GetMemberByUserNameDto.Response> {
        val result = getMemberByUserNameUseCase.execute(
            GetMemberByUserNameDto.Request(username = username)
        ).getOrThrow()
        return ApiResponse.success(result)
    }
}

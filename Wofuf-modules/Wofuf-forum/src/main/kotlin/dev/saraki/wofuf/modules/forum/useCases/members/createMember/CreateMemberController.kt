package dev.saraki.wofuf.modules.forum.useCases.members.createMember

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/25 15:10
 *   @description:
 */
@RestController
@RequestMapping(ForumApiConstantV1.Members.ROOT)
class CreateMemberController : BaseController() {
    @Autowired
    private lateinit var createMemberUseCase: CreateMemberUseCase

    @PostMapping()
    fun createMember(
        @RequestBody request: CreateMemberDto.Request
    ): ApiResponse<Unit> {
        val result = createMemberUseCase.execute(request)
        return ApiResponse.success(Unit)
    }
}
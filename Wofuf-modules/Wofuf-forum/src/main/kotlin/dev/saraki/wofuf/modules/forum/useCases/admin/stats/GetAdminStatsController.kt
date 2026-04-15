package dev.saraki.wofuf.modules.forum.useCases.admin.stats

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(ForumApiConstantV1.Admin.ROOT)
class GetAdminStatsController(
    private val getAdminStatsUseCase: GetAdminStatsUseCase
) : BaseController() {

    @GetMapping("/stats")
    fun getAdminStats(): ApiResponse<GetAdminStatsDto.Response> {
        val result = getAdminStatsUseCase.execute(Unit)
        return if (result.isSuccess) {
            ApiResponse.success(result.getOrThrow())
        } else {
            ApiResponse.error(result.exceptionOrNull()?.message ?: "Unknown error")
        }
    }
}

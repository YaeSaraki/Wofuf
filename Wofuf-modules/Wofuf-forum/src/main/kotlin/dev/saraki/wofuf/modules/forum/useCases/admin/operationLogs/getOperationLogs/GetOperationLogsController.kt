package dev.saraki.wofuf.modules.forum.useCases.admin.operationLogs.getOperationLogs

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.modules.forum.useCases.admin.operationLogs.getOperationLogs.GetOperationLogsDto.Request
import dev.saraki.wofuf.modules.forum.useCases.admin.operationLogs.getOperationLogs.GetOperationLogsDto.Response
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.web.bind.annotation.*

/**
 * 获取操作日志 Controller
 */
@RestController
@RequestMapping(ForumApiConstantV1.Admin.LOGS)
class GetOperationLogsController(
    private val getOperationLogsUseCase: GetOperationLogsUseCase
) : BaseController() {

    @GetMapping
    fun getOperationLogs(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) operatorId: String?,
        @RequestParam(required = false) operationType: String?,
        @RequestParam(required = false) targetType: String?
    ): ApiResponse<Response> {
        val request = Request(
            page = page,
            size = size,
            operatorId = operatorId,
            operationType = operationType,
            targetType = targetType
        )

        val result = getOperationLogsUseCase.execute(request).getOrThrow()
        return ApiResponse.success(result)
    }
}

package dev.saraki.wofuf.modules.forum.useCases.admin.operationLogs.getOperationLogs

/**
 * 获取操作日志 DTO
 */
class GetOperationLogsDto {

    /**
     * 请求参数
     */
    data class Request(
        val page: Int = 0,
        val size: Int = 20,
        val operatorId: String? = null,       // 按操作者 ID 筛选
        val operationType: String? = null,     // 按操作类型筛选
        val targetType: String? = null         // 按目标类型筛选 (POST, COMMENT, MEMBER, IMAGE)
    )

    /**
     * 单条日志响应
     */
    data class LogEntry(
        val logId: String,
        val operationType: String,
        val operationName: String,           // 中文操作名称
        val targetType: String,
        val targetId: String,
        val operatorId: String,
        val operatorNickname: String?,
        val details: String?,
        val createdAt: Long                  // 时间戳
    )

    /**
     * 响应
     */
    data class Response(
        val logs: List<LogEntry>,
        val total: Long,
        val page: Int,
        val size: Int,
        val totalPages: Int
    )
}

package dev.saraki.wofuf.modules.forum.useCases.admin.operationLogs.getOperationLogs

import dev.saraki.wofuf.modules.forum.domain.OperationType
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
import dev.saraki.wofuf.modules.forum.infra.repos.OperationLogRepo
import dev.saraki.wofuf.modules.forum.infra.repos.jpa.entities.OperationLogEntity
import dev.saraki.wofuf.modules.forum.useCases.admin.operationLogs.getOperationLogs.GetOperationLogsDto.Request
import dev.saraki.wofuf.modules.forum.useCases.admin.operationLogs.getOperationLogs.GetOperationLogsDto.Response
import dev.saraki.wofuf.modules.forum.useCases.admin.operationLogs.getOperationLogs.GetOperationLogsDto.LogEntry
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import org.springframework.stereotype.Service
import java.time.ZoneId

/**
 * 获取操作日志 UseCase
 */
@Service
class GetOperationLogsUseCase(
    private val operationLogRepo: OperationLogRepo
) : UseCase<Request, Response> {

    override fun execute(request: Request): Result<Response> {
        val safeSize = request.size.coerceIn(1, 100)

        // 转换操作类型
        val operationType = request.operationType?.let {
            try {
                OperationType.valueOf(it.uppercase())
            } catch (e: IllegalArgumentException) {
                null
            }
        }

        // 转换操作者 ID（仅在提供了有效 UUID 时才按 ID 筛选）
        val operatorId = request.operatorId?.let { operatorIdStr ->
            try {
                MemberId.create(dev.saraki.wofuf.shared.domain.UniqueEntityId(operatorIdStr)).getOrNull()
            } catch (e: IllegalArgumentException) {
                null // 无效的 UUID，返回 null 表示不按操作者 ID 筛选
            }
        }

        // 查询日志（使用带 member 的查询以获取 nickname）
        val logs = operationLogRepo.findByConditionsWithMember(
            operatorId = operatorId,
            operationType = operationType,
            targetType = request.targetType,
            page = request.page,
            size = safeSize
        )

        // 统计总数（如果有任何筛选条件，使用条件统计）
        val total = if (operatorId != null || operationType != null || request.targetType != null) {
            operationLogRepo.countByConditions(
                operatorId = operatorId,
                operationType = operationType,
                targetType = request.targetType
            )
        } else {
            operationLogRepo.count()
        }

        // 构建响应
        val logEntries = logs.map { entity -> entity.toLogEntry() }

        return Result.success(
            Response(
                logs = logEntries,
                total = total,
                page = request.page,
                size = safeSize,
                totalPages = kotlin.math.ceil(total.toDouble() / safeSize).toInt()
            )
        )
    }

    private fun OperationLogEntity.toLogEntry(): LogEntry {
        return LogEntry(
            logId = this.logId,
            operationType = this.operationType,
            operationName = getOperationName(OperationType.valueOf(this.operationType)),
            targetType = this.targetType,
            targetId = this.targetId,
            operatorId = this.operatorMember.memberId,
            operatorNickname = this.operatorMember.nickname,
            details = this.details,
            createdAt = this.createdAt?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli() ?: 0L
        )
    }

    private fun getOperationName(type: OperationType): String {
        return when (type) {
            OperationType.POST_PIN -> "置顶帖子"
            OperationType.POST_UNPIN -> "取消置顶"
            OperationType.POST_FEATURE -> "加精帖子"
            OperationType.POST_UNFEATURE -> "取消加精"
            OperationType.POST_HIDE -> "隐藏帖子"
            OperationType.POST_SHOW -> "显示帖子"
            OperationType.POST_SET_REVIEW -> "设为待审核"
            OperationType.POST_APPROVE -> "审核通过"
            OperationType.POST_DELETE -> "删除帖子"
            OperationType.POST_EDIT -> "编辑帖子"
            OperationType.COMMENT_HIDE -> "隐藏评论"
            OperationType.COMMENT_SHOW -> "显示评论"
            OperationType.COMMENT_DELETE -> "删除评论"
            OperationType.COMMENT_EDIT -> "编辑评论"
            OperationType.MEMBER_BAN -> "封禁用户"
            OperationType.MEMBER_UNBAN -> "解封用户"
            OperationType.MEMBER_GRANT_PERMISSION -> "授予权限"
            OperationType.MEMBER_REVOKE_PERMISSION -> "撤销权限"
            OperationType.IMAGE_DELETE -> "删除图片"
            OperationType.UNKNOWN -> "未知操作"
        }
    }
}

package dev.saraki.wofuf.modules.forum.infra.services

import dev.saraki.wofuf.modules.forum.domain.OperationLog
import dev.saraki.wofuf.modules.forum.domain.OperationType
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
import dev.saraki.wofuf.modules.forum.infra.repos.OperationLogRepo
import org.springframework.stereotype.Service

/**
 * 操作日志服务
 * 用于在其他 UseCase 中记录操作日志
 */
@Service
class OperationLogService(
    private val operationLogRepo: OperationLogRepo
) {

    /**
     * 记录操作日志
     */
    fun log(
        operationType: OperationType,
        targetType: String,
        targetId: String,
        operatorId: MemberId,
        details: String? = null
    ): OperationLog {
        val log = OperationLog.create(
            operationType = operationType,
            targetType = targetType,
            targetId = targetId,
            operatorId = operatorId,
            details = details
        )
        return operationLogRepo.save(log)
    }

    /**
     * 记录帖子操作日志
     */
    fun logPostAction(
        operationType: OperationType,
        postId: String,
        operatorId: MemberId,
        details: String? = null
    ): OperationLog {
        return log(
            operationType = operationType,
            targetType = "POST",
            targetId = postId,
            operatorId = operatorId,
            details = details
        )
    }

    /**
     * 记录评论操作日志
     */
    fun logCommentAction(
        operationType: OperationType,
        commentId: String,
        operatorId: MemberId,
        details: String? = null
    ): OperationLog {
        return log(
            operationType = operationType,
            targetType = "COMMENT",
            targetId = commentId,
            operatorId = operatorId,
            details = details
        )
    }

    /**
     * 记录成员操作日志
     */
    fun logMemberAction(
        operationType: OperationType,
        memberId: MemberId,
        operatorId: MemberId,
        details: String? = null
    ): OperationLog {
        return log(
            operationType = operationType,
            targetType = "MEMBER",
            targetId = memberId.stringValue,
            operatorId = operatorId,
            details = details
        )
    }

    /**
     * 记录图片操作日志
     */
    fun logImageAction(
        operationType: OperationType,
        imageId: String,
        operatorId: MemberId,
        details: String? = null
    ): OperationLog {
        return log(
            operationType = operationType,
            targetType = "IMAGE",
            targetId = imageId,
            operatorId = operatorId,
            details = details
        )
    }
}

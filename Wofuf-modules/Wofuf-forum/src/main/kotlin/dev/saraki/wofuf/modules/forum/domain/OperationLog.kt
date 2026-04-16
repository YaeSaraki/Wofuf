package dev.saraki.wofuf.modules.forum.domain

import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
import dev.saraki.wofuf.shared.domain.AggregateRoot
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import java.time.LocalDateTime

/**
 * 操作日志类型枚举
 */
enum class OperationType {
    // 帖子操作
    POST_PIN,
    POST_UNPIN,
    POST_FEATURE,
    POST_UNFEATURE,
    POST_HIDE,
    POST_SHOW,
    POST_SET_REVIEW,
    POST_APPROVE,
    POST_DELETE,
    POST_EDIT,

    // 评论操作
    COMMENT_HIDE,
    COMMENT_SHOW,
    COMMENT_DELETE,
    COMMENT_EDIT,

    // 成员操作
    MEMBER_BAN,
    MEMBER_UNBAN,
    MEMBER_GRANT_PERMISSION,
    MEMBER_REVOKE_PERMISSION,

    // 图片操作
    IMAGE_DELETE,

    // 其他
    UNKNOWN
}

/**
 * 操作日志实体
 */
data class OperationLogProps(
    val operationType: OperationType,
    val targetType: String,        // POST, COMMENT, MEMBER, IMAGE
    val targetId: String,          // 被操作对象的 ID
    val operatorId: MemberId,      // 操作者 Member ID
    val details: String?,           // 详细信息（如原因等）
    val createdAt: LocalDateTime
)

class OperationLog private constructor(
    props: OperationLogProps,
    id: UniqueEntityId?
) : AggregateRoot<OperationLogProps>(props, id) {

    val logId: String
        get() = _id.uuid.toString()

    val operationType: OperationType
        get() = props.operationType

    val targetType: String
        get() = props.targetType

    val targetId: String
        get() = props.targetId

    val operatorId: MemberId
        get() = props.operatorId

    val details: String?
        get() = props.details

    val createdAt: LocalDateTime
        get() = props.createdAt

    companion object {
        fun create(
            operationType: OperationType,
            targetType: String,
            targetId: String,
            operatorId: MemberId,
            details: String? = null,
            createdAt: LocalDateTime = LocalDateTime.now()
        ): OperationLog {
            val props = OperationLogProps(
                operationType = operationType,
                targetType = targetType,
                targetId = targetId,
                operatorId = operatorId,
                details = details,
                createdAt = createdAt
            )
            return OperationLog(props, null)
        }
    }
}

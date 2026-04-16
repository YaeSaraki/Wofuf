package dev.saraki.wofuf.modules.forum.infra.repos.jpa.mappers

import dev.saraki.wofuf.modules.forum.domain.OperationLog
import dev.saraki.wofuf.modules.forum.domain.OperationType
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
import dev.saraki.wofuf.modules.forum.infra.repos.jpa.entities.MemberEntity
import dev.saraki.wofuf.modules.forum.infra.repos.jpa.entities.OperationLogEntity
import dev.saraki.wofuf.shared.domain.UniqueEntityId

/**
 * OperationLogEntity 与 Domain 实体映射器
 */
object OperationLogEntityMapper {

    fun toDomain(entity: OperationLogEntity): OperationLog {
        val memberId = MemberId.create(UniqueEntityId(entity.operatorMember.memberId)).getOrThrow()
        val operationType = OperationType.valueOf(entity.operationType)

        return OperationLog.create(
            operationType = operationType,
            targetType = entity.targetType,
            targetId = entity.targetId,
            operatorId = memberId,
            details = entity.details,
            createdAt = entity.createdAt ?: java.time.LocalDateTime.now()
        )
    }

    /**
     * 获取操作者昵称（从已加载的 MemberEntity 关系中获取）
     */
    fun getOperatorNickname(entity: OperationLogEntity): String {
        return entity.operatorMember.nickname
    }

    fun toEntity(log: OperationLog, operatorMember: MemberEntity): OperationLogEntity {
        return OperationLogEntity(
            logId = java.util.UUID.randomUUID().toString(),
            operationType = log.operationType.name,
            targetType = log.targetType,
            targetId = log.targetId,
            operatorMember = operatorMember,
            details = log.details
        )
    }
}

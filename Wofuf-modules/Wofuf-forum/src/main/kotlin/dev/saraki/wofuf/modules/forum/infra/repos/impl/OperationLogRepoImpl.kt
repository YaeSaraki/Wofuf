package dev.saraki.wofuf.modules.forum.infra.repos.impl

import dev.saraki.wofuf.modules.forum.domain.OperationLog
import dev.saraki.wofuf.modules.forum.domain.OperationType
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
import dev.saraki.wofuf.modules.forum.infra.repos.MemberRepo
import dev.saraki.wofuf.modules.forum.infra.repos.OperationLogRepo
import dev.saraki.wofuf.modules.forum.infra.repos.jpa.MemberJpaRepo
import dev.saraki.wofuf.modules.forum.infra.repos.jpa.OperationLogJpaRepo
import dev.saraki.wofuf.modules.forum.infra.repos.jpa.entities.OperationLogEntity
import dev.saraki.wofuf.modules.forum.infra.repos.jpa.mappers.OperationLogEntityMapper
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository

/**
 * 操作日志仓库实现
 */
@Repository
class OperationLogRepoImpl(
    private val operationLogJpaRepo: OperationLogJpaRepo,
    private val memberJpaRepo: MemberJpaRepo,
    private val memberRepo: MemberRepo
) : OperationLogRepo {

    override fun save(log: OperationLog): OperationLog {
        val operatorMember = memberJpaRepo.findById(log.operatorId.stringValue)
            .orElseThrow { IllegalStateException("Operator member not found: ${log.operatorId.stringValue}") }
        val entity = OperationLogEntityMapper.toEntity(log, operatorMember)
        val saved = operationLogJpaRepo.save(entity)
        return OperationLogEntityMapper.toDomain(saved)
    }

    override fun findLogs(page: Int, size: Int): List<OperationLog> {
        val safeSize = size.coerceAtLeast(1)
        return operationLogJpaRepo.findAllByOrderByCreatedAtDesc(PageRequest.of(page, safeSize))
            .content
            .map(OperationLogEntityMapper::toDomain)
    }

    override fun findByOperatorId(operatorId: MemberId, page: Int, size: Int): List<OperationLog> {
        val safeSize = size.coerceAtLeast(1)
        return operationLogJpaRepo.findByOperatorMemberMemberIdOrderByCreatedAtDesc(
            operatorId.stringValue,
            PageRequest.of(page, safeSize)
        ).content.map(OperationLogEntityMapper::toDomain)
    }

    override fun findByOperationType(operationType: OperationType, page: Int, size: Int): List<OperationLog> {
        val safeSize = size.coerceAtLeast(1)
        return operationLogJpaRepo.findByOperationTypeOrderByCreatedAtDesc(
            operationType.name,
            PageRequest.of(page, safeSize)
        ).content.map(OperationLogEntityMapper::toDomain)
    }

    override fun findByTarget(targetType: String, targetId: String, page: Int, size: Int): List<OperationLog> {
        val safeSize = size.coerceAtLeast(1)
        return operationLogJpaRepo.findByTargetTypeAndTargetIdOrderByCreatedAtDesc(
            targetType,
            targetId,
            PageRequest.of(page, safeSize)
        ).content.map(OperationLogEntityMapper::toDomain)
    }

    override fun findByConditions(
        operatorId: MemberId?,
        operationType: OperationType?,
        targetType: String?,
        page: Int,
        size: Int
    ): List<OperationLog> {
        val safeSize = size.coerceAtLeast(1)
        return operationLogJpaRepo.findByConditions(
            operatorMemberId = operatorId?.stringValue,
            operationType = operationType?.name,
            targetType = targetType,
            pageable = PageRequest.of(page, safeSize)
        ).content.map(OperationLogEntityMapper::toDomain)
    }

    override fun findByConditionsWithMember(
        operatorId: MemberId?,
        operationType: OperationType?,
        targetType: String?,
        page: Int,
        size: Int
    ): List<OperationLogEntity> {
        val safeSize = size.coerceAtLeast(1)
        return operationLogJpaRepo.findByConditions(
            operatorMemberId = operatorId?.stringValue,
            operationType = operationType?.name,
            targetType = targetType,
            pageable = PageRequest.of(page, safeSize)
        ).content
    }

    override fun count(): Long =
        operationLogJpaRepo.count()

    override fun countByConditions(
        operatorId: MemberId?,
        operationType: OperationType?,
        targetType: String?
    ): Long = operationLogJpaRepo.countByConditions(
        operatorMemberId = operatorId?.stringValue,
        operationType = operationType?.name,
        targetType = targetType
    )

    override fun countByOperatorId(operatorId: MemberId): Long =
        operationLogJpaRepo.findByOperatorMemberMemberIdOrderByCreatedAtDesc(
            operatorId.stringValue,
            PageRequest.of(0, 1)
        ).totalElements

    override fun countByOperationType(operationType: OperationType): Long =
        operationLogJpaRepo.findByOperationTypeOrderByCreatedAtDesc(
            operationType.name,
            PageRequest.of(0, 1)
        ).totalElements
}

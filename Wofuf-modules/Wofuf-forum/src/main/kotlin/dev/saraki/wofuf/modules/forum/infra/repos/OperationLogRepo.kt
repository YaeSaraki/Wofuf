package dev.saraki.wofuf.modules.forum.infra.repos

import dev.saraki.wofuf.modules.forum.domain.OperationLog
import dev.saraki.wofuf.modules.forum.domain.OperationType
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId

/**
 * 操作日志仓库接口
 */
interface OperationLogRepo {

    /**
     * 保存日志
     */
    fun save(log: OperationLog): OperationLog

    /**
     * 分页获取所有日志
     */
    fun findLogs(page: Int, size: Int): List<OperationLog>

    /**
     * 根据操作者 ID 获取日志
     */
    fun findByOperatorId(operatorId: MemberId, page: Int, size: Int): List<OperationLog>

    /**
     * 根据操作类型获取日志
     */
    fun findByOperationType(operationType: OperationType, page: Int, size: Int): List<OperationLog>

    /**
     * 根据目标类型和目标 ID 获取日志
     */
    fun findByTarget(targetType: String, targetId: String, page: Int, size: Int): List<OperationLog>

    /**
     * 复合条件查询
     */
    fun findByConditions(
        operatorId: MemberId?,
        operationType: OperationType?,
        targetType: String?,
        page: Int,
        size: Int
    ): List<OperationLog>

    /**
     * 复合条件查询（返回实体以获取关联数据）
     */
    fun findByConditionsWithMember(
        operatorId: MemberId?,
        operationType: OperationType?,
        targetType: String?,
        page: Int,
        size: Int
    ): List<dev.saraki.wofuf.modules.forum.infra.repos.jpa.entities.OperationLogEntity>

    /**
     * 统计总数
     */
    fun count(): Long

    /**
     * 复合条件统计
     */
    fun countByConditions(
        operatorId: MemberId?,
        operationType: OperationType?,
        targetType: String?
    ): Long

    /**
     * 根据操作者 ID 统计
     */
    fun countByOperatorId(operatorId: MemberId): Long

    /**
     * 根据操作类型统计
     */
    fun countByOperationType(operationType: OperationType): Long
}

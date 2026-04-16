package dev.saraki.wofuf.modules.forum.infra.repos.jpa

import dev.saraki.wofuf.modules.forum.infra.repos.jpa.entities.OperationLogEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * 操作日志 JPA 仓库
 */
@Repository
interface OperationLogJpaRepo : JpaRepository<OperationLogEntity, String> {

    /**
     * 分页查询所有日志，按时间倒序
     */
    fun findAllByOrderByCreatedAtDesc(pageable: Pageable): Page<OperationLogEntity>

    /**
     * 根据操作者 ID 查询日志
     */
    fun findByOperatorMemberMemberIdOrderByCreatedAtDesc(operatorMemberId: String, pageable: Pageable): Page<OperationLogEntity>

    /**
     * 根据操作类型查询日志
     */
    fun findByOperationTypeOrderByCreatedAtDesc(operationType: String, pageable: Pageable): Page<OperationLogEntity>

    /**
     * 根据目标类型和目标 ID 查询日志
     */
    fun findByTargetTypeAndTargetIdOrderByCreatedAtDesc(
        targetType: String,
        targetId: String,
        pageable: Pageable
    ): Page<OperationLogEntity>

    /**
     * 复合条件查询（带 LEFT JOIN FETCH 加载 operatorMember）
     */
    @Query("""
        SELECT o FROM OperationLogEntity o
        LEFT JOIN FETCH o.operatorMember om
        WHERE (:operatorMemberId IS NULL OR om.memberId = :operatorMemberId)
        AND (:operationType IS NULL OR o.operationType = :operationType)
        AND (:targetType IS NULL OR o.targetType = :targetType)
        ORDER BY o.createdAt DESC
    """)
    fun findByConditions(
        @Param("operatorMemberId") operatorMemberId: String?,
        @Param("operationType") operationType: String?,
        @Param("targetType") targetType: String?,
        pageable: Pageable
    ): Page<OperationLogEntity>

    /**
     * 复合条件统计
     */
    @Query("""
        SELECT COUNT(o) FROM OperationLogEntity o
        WHERE (:operatorMemberId IS NULL OR o.operatorMember.memberId = :operatorMemberId)
        AND (:operationType IS NULL OR o.operationType = :operationType)
        AND (:targetType IS NULL OR o.targetType = :targetType)
    """)
    fun countByConditions(
        @Param("operatorMemberId") operatorMemberId: String?,
        @Param("operationType") operationType: String?,
        @Param("targetType") targetType: String?
    ): Long
}

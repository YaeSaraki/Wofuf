package dev.saraki.wofuf.modules.forum.infra.repos.jpa.entities

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

/**
 * 操作日志 JPA 实体
 */
@Entity
@Table(name = "operation_log", indexes = [
    Index(name = "idx_operation_log_operator", columnList = "operator_member_id"),
    Index(name = "idx_operation_log_target", columnList = "target_type, target_id"),
    Index(name = "idx_operation_log_type", columnList = "operation_type"),
    Index(name = "idx_operation_log_created_at", columnList = "created_at")
])
data class OperationLogEntity(
    @Id
    @Column(name = "log_id", nullable = false)
    val logId: String,

    @Column(name = "operation_type", nullable = false)
    val operationType: String,

    @Column(name = "target_type", nullable = false)
    val targetType: String,

    @Column(name = "target_id", nullable = false)
    val targetId: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_member_id", nullable = false)
    val operatorMember: MemberEntity,

    @Column(name = "details", columnDefinition = "TEXT")
    val details: String?,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime? = null
)

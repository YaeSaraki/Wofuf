package dev.saraki.wofuf.modules.players.infra.repos.jpa.entities

import dev.saraki.wofuf.modules.forum.infra.repos.jpa.entities.MemberEntity
import jakarta.persistence.*
import org.hibernate.annotations.DynamicUpdate
import java.time.LocalDateTime

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/19 15:56
 *   @description:
 */

@Entity
@DynamicUpdate
@Table(name = "players")
data class PlayerEntity(
    @Id
    @Column(name = "player_id", unique = true, nullable = false)
    val playerId: String,

    @Column(name = "playerName", nullable = false, unique = true)
    val playerName: String,

    @Column(name = "first_login", nullable = false)
    val firstLogin: Long,

    @Column(name = "last_login", nullable = false)
    val lastLogin: Long,

    @Column(name = "total_playtime", nullable = false)
    val totalPlaytime: Long,

    @Column(name = "update_time", nullable = false)
    val updateTime: Long,

    @Column(name = "advancements_json", columnDefinition = "LONGTEXT", nullable = true)
    val advancementsJson: String?,

    @Column(name = "statistics_json", columnDefinition = "LONGTEXT", nullable = true)
    val statisticsJson: String?,

    @Embedded
    val playerSkin: PlayerSkinEntity?,

    // 与MemberEntity的一对一关系
    @OneToOne(mappedBy = "playerEntity", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val memberEntity: MemberEntity? = null,

    @Column(name = "created_at", nullable = true, insertable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = true, insertable = false, updatable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
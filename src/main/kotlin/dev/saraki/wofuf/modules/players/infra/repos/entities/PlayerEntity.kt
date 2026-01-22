package dev.saraki.wofuf.modules.players.infra.repos.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/19 15:56
 *   @description:
 */


@Entity
@Table(name = "players")
class PlayerEntity(
    @Id
    var uuid: String? = null,

    var name: String? = null,

    var firstLogin: Long? = null,

    var lastLogin: Long? = null,

    var totalPlaytime: Long? = null,

    var updateTime: Long? = null,

    @Column(name = "advancements_json", columnDefinition = "LONGTEXT")
    var advancementsJson: String? = null,

    @Column(name = "statistics_json", columnDefinition = "LONGTEXT")
    val statisticsJson: String? = null
)
package dev.saraki.wofuf.modules.players.infra.repos.jpa.entities

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/2 15:52
 *   @description:
 */

@Embeddable
data class PlayerSkinEntity(
    @Column(name = "skin", columnDefinition = "LONGTEXT", nullable = false)
    val skin: String,

    @Column(name = "cape", columnDefinition = "LONGTEXT", nullable = true)
    val cape: String?,

    @Column(name = "type", length = 10, nullable = true)
    val type: String?
)
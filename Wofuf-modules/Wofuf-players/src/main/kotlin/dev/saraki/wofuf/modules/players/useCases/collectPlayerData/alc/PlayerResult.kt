package dev.saraki.wofuf.modules.players.useCases.collectPlayerData.alc

import java.util.*

data class PlayerResult(
    val uuid: UUID,
    val name: String,
    val online: Boolean,
    val world: String,
    val firstLogin: Long,
    val lastLogin: Long,
    val totalPlaytimeSeconds: Long,
    val x: Double?,
    val y: Double?,
    val z: Double?
)
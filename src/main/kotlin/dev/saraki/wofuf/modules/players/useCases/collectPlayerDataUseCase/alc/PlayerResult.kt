package dev.saraki.wofuf.modules.players.useCases.collectPlayerDataUseCase.alc

import java.util.UUID

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
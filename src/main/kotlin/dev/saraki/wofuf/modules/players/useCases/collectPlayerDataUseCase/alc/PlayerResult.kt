package dev.saraki.wofuf.modules.players.useCases.collectPlayerDataUseCase.alc

data class PlayerResult(
    val uuid: String,
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
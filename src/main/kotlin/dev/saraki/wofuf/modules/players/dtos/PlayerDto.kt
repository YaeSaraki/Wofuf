package dev.saraki.wofuf.modules.players.dtos

data class PlayerDto(
    val uuid: String,
    val name: String,

    val firstLogin: Long,
    val lastLogin: Long?,
    val totalPlaytimeSeconds: Long,
    val updateTime: Long,

    val statistics: Map<String, PlayerStatisticDto>,
    val advancements: List<PlayerAdvancementDto>
)
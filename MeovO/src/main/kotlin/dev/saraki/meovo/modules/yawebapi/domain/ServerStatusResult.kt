package dev.saraki.meovo.modules.yawebapi.domain

/**
 * 封装服务器状态结果
 */
data class ServerStatusResult(
    val onlinePlayers: Int,
    val maxPlayers: Int,
    val tps: Double,
    val heartbeatStatus: Boolean,
    val updateTime: Long
)
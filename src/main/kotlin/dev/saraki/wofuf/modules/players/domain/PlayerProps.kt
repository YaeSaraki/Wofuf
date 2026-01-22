package dev.saraki.wofuf.modules.players.domain

import dev.saraki.wofuf.shared.domain.ValueObject


data class PlayerProps(
    val name: String,
    val firstLogin: Long,
    val lastLogin: Long,
    val totalPlaytimeSeconds: Long,
    val updateTime: Long
): ValueObject<PlayerProps>() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PlayerProps
        return name == other.name && firstLogin == other.firstLogin && lastLogin == other.lastLogin && totalPlaytimeSeconds == other.totalPlaytimeSeconds
    }

    override fun hashCode(): Int {
        return name.hashCode() * 31 + firstLogin.hashCode() * 31 + lastLogin.hashCode() * 31 + totalPlaytimeSeconds.hashCode() * 31
    }

    override fun toString(): String {
        return "PlayerProps(name='$name', firstLogin=$firstLogin, lastLogin=$lastLogin, totalPlaytimeSeconds=$totalPlaytimeSeconds)"
    }
}

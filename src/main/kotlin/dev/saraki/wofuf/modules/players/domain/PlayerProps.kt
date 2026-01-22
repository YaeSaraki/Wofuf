package dev.saraki.wofuf.modules.players.domain

import dev.saraki.wofuf.shared.domain.ValueObject
import dev.saraki.wofuf.shared.core.Result
import java.util.Base64


data class PlayerProps(
    val name: String,
    val firstLogin: Long,
    val lastLogin: Long,
    val totalPlaytimeSeconds: Long,
    val updateTime: Long,
): ValueObject<PlayerProps>() {

    companion object {
        fun create(
            name: String,
            firstLogin: Long,
            lastLogin: Long,
            totalPlaytimeSeconds: Long,
            updateTime: Long,
        ): Result.Success<PlayerProps> {
            return Result.success(
                PlayerProps(
                    name = name,
                    firstLogin = firstLogin,
                    lastLogin = lastLogin,
                    totalPlaytimeSeconds = totalPlaytimeSeconds,
                    updateTime = updateTime,
                )
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PlayerProps
        return name == other.name && updateTime == other.updateTime
    }

    override fun hashCode(): Int {
        return name.hashCode() * 31 + updateTime.hashCode() * 31
    }

    override fun toString(): String {
        return "PlayerProps(name='$name', firstLogin=$firstLogin, lastLogin=$lastLogin, totalPlaytimeSeconds=$totalPlaytimeSeconds, updateTime=$updateTime)"
    }
}

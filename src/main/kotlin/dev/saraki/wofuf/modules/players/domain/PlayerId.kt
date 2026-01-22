package dev.saraki.wofuf.modules.players.domain

import dev.saraki.wofuf.shared.domain.UniqueEntityId
import dev.saraki.wofuf.shared.domain.ValueObject
import dev.saraki.wofuf.shared.core.Result

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/19 15:21
 *   @description:
 */
class PlayerId(val value: UniqueEntityId) : ValueObject<PlayerId>() {
    companion object {
        fun create(): Result<PlayerId> {
            return Result.success(PlayerId(UniqueEntityId()))
        }
        fun create(id: String): Result<PlayerId> {
            return Result.success(PlayerId(UniqueEntityId(id)))
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PlayerId
        return value == other.value
    }
    override fun hashCode(): Int = value.hashCode()
    override fun toString(): String = value.toString()
}
package dev.saraki.wofuf.modules.players.domain

import dev.saraki.wofuf.shared.domain.ValueObject

class PlayerStatistic(
    val category: String,
    val key: String,
    val value: Long
): ValueObject<PlayerStatistic>() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PlayerStatistic
        return category == other.category && key == other.key && value == other.value
    }

    override fun hashCode(): Int {
        return category.hashCode() * 31 + key.hashCode() * 31 + value.hashCode() * 31
    }

    override fun toString(): String {
        return "PlayerStatistic(category='$category', key='$key', value=$value)"
    }
}

package dev.saraki.wofuf.modules.players.domain

import dev.saraki.wofuf.shared.domain.ValueObject

class PlayerAdvancement(
    val key: String,
    val done: Boolean,
    val completed: List<String>,
    val remaining: List<String>
) : ValueObject<PlayerAdvancement>() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PlayerAdvancement
        return key == other.key && done == other.done && completed == other.completed && remaining == other.remaining
    }

    override fun hashCode(): Int {
        return key.hashCode() * 31 + done.hashCode() * 31 + completed.hashCode() * 31 + remaining.hashCode() * 31
    }

    override fun toString(): String {
        return "PlayerAdvancement(key='$key', done=$done, completed=$completed, remaining=$remaining)"
    }
}

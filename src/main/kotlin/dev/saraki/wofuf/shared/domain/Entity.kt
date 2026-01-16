package dev.saraki.wofuf.shared.domain

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 21:13
 *   @description:
 */
abstract class Entity<T>(val props: T, id: UniqueEntityId? = null) {
    protected open val _id: UniqueEntityId = id ?: UniqueEntityId()

    fun equals(other: Entity<T>?): Boolean {
        if (other == null) return false
        if (this === other) return true
        return this._id == other._id
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Entity<*>
        return _id == other._id
    }

    override fun hashCode(): Int {
        return _id.hashCode()
    }
}
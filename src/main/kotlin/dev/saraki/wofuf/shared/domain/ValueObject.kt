package dev.saraki.wofuf.shared.domain

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 16:06
 *   @description:
 */
abstract class ValueObject<T> {
    abstract override fun equals(other: Any?): Boolean
    abstract override fun hashCode(): Int
    abstract override fun toString(): String
}
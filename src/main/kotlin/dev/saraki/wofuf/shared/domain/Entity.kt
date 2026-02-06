package dev.saraki.wofuf.shared.domain

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 21:13
 *   @description:
 */
abstract class Entity<T>(
    protected val props: T,
    protected val id: UniqueEntityId?
) {
    val _id: UniqueEntityId
        get() = id ?: UniqueEntityId()
}
package dev.saraki.wofuf.shared.domain.events

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 21:23
 *   @description:
 */
interface IHandle<T : IDomainEvent> {
    fun setupSubscription(): Unit
}
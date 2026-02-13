package dev.saraki.wofuf.shared.domain.events

import dev.saraki.wofuf.shared.domain.AggregateRoot

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 21:03
 *   @description:
 */

interface IDomainEvents {
    fun publish(domainEvent: IDomainEvent)
    fun publishAll(aggregate: AggregateRoot<*>)
    fun <T : IDomainEvent> subscribe(handler: IDomainEventHandler<T>)
    fun <T : IDomainEvent> handleEvent(event: T)
}
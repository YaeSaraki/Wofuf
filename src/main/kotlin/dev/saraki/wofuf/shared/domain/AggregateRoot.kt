package dev.saraki.wofuf.shared.domain

import dev.saraki.wofuf.shared.domain.events.DomainEvents
import dev.saraki.wofuf.shared.domain.events.IDomainEvent

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 11:21
 *   @description:
 */
abstract class AggregateRoot<T>(props: T, id: UniqueEntityId? = null) : Entity<T>(props, id) {
    private val _domainEvents: MutableList<IDomainEvent> = mutableListOf()

    public override val _id: UniqueEntityId
        get() = _id

    val domainEvents: List<IDomainEvent>
        get() = _domainEvents.toList() // 返回不可变副本

    protected fun addDomainEvent(domainEvent: IDomainEvent) {
        _domainEvents.add(domainEvent)
        DomainEvents.markAggregateForDispatch(this)
        logDomainEventAdded(domainEvent)
    }

    fun clearEvents() {
        _domainEvents.clear()
    }

    private fun logDomainEventAdded(domainEvent: IDomainEvent) {
        val thisClassName = this::class.simpleName
        val domainEventClassName = domainEvent::class.simpleName
        println("[Domain Event Created]: $thisClassName ==> $domainEventClassName")
    }
}
package dev.saraki.wofuf.shared.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import dev.saraki.wofuf.shared.domain.events.IDomainEvent

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 11:21
 *   @description:
 */
abstract class AggregateRoot<T>(
    props: T,
    id: UniqueEntityId? = null
) : Entity<T>(props, id) {

    @JsonIgnore
    private val domainEvents: MutableList<IDomainEvent> = mutableListOf()

    protected fun addDomainEvent(domainEvent: IDomainEvent) {
        domainEvents.add(domainEvent)
        logDomainEventAdded(domainEvent)
    }

    fun clearEvents() {
        domainEvents.clear()
    }

    fun getDomainEvents(): List<IDomainEvent> {
        return domainEvents.toList()
    }

    private fun logDomainEventAdded(domainEvent: IDomainEvent) {
        val thisClassName = this::class.simpleName
        val domainEventClassName = domainEvent::class.simpleName
        println("[Domain Event Created]: $thisClassName ==> $domainEventClassName")
    }
}

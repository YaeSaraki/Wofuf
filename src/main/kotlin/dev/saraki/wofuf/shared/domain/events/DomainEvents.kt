package dev.saraki.wofuf.shared.domain.events

import dev.saraki.wofuf.shared.domain.AggregateRoot
import dev.saraki.wofuf.shared.domain.UniqueEntityId

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 21:03
 *   @description:
 */
object DomainEvents {

    private val handlersMap: MutableMap<String, MutableList<(IDomainEvent) -> Unit>> = mutableMapOf()
    private val markedAggregates: MutableList<AggregateRoot<*>> = mutableListOf()

    /**
     * @method markAggregateForDispatch
     * @static
     * @desc Called by aggregate root objects that have created domain
     * events to eventually be dispatched when the infrastructure commits
     * the unit of work.
     */
    fun markAggregateForDispatch(aggregate: AggregateRoot<*>) {
        val aggregateFound = findMarkedAggregateByID(aggregate._id) != null
        if (!aggregateFound) {
            markedAggregates.add(aggregate)
        }
    }


    private fun dispatchAggregateEvents(aggregate: AggregateRoot<*>) {
        aggregate.domainEvents.forEach { event -> dispatch(event) }
    }

    private fun removeAggregateFromMarkedDispatchList(aggregate: AggregateRoot<*>) {
        markedAggregates.removeIf { it._id == aggregate._id }
    }

    private fun findMarkedAggregateByID(id: UniqueEntityId): AggregateRoot<*>? {
        return markedAggregates.find { it._id == id }
    }

    fun dispatchEventsForAggregate(id: UniqueEntityId) {
        val aggregate = findMarkedAggregateByID(id)
        aggregate?.let {
            dispatchAggregateEvents(it)
            it.clearEvents()
            removeAggregateFromMarkedDispatchList(it)
        }
    }

    fun register(callback: (IDomainEvent) -> Unit, eventClassName: String) {
        handlersMap.computeIfAbsent(eventClassName) { mutableListOf() }.add(callback)
    }

    fun clearHandlers() {
        handlersMap.clear()
    }

    fun clearMarkedAggregates() {
        markedAggregates.clear()
    }

    private fun dispatch(event: IDomainEvent) {
        val eventClassName = event::class.simpleName ?: ""
        handlersMap[eventClassName]?.forEach { handler ->
            handler(event)
        }
    }
}
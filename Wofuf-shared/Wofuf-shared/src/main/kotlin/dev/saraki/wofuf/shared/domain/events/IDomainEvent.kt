package dev.saraki.wofuf.shared.domain.events

import dev.saraki.wofuf.shared.domain.UniqueEntityId
import java.time.LocalDateTime

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 21:08
 *   @description:
 */
abstract class IDomainEvent(
    private val aggregateId: UniqueEntityId,
) {
    open val dataTimeOccurred: LocalDateTime = LocalDateTime.now()

    fun getAggregateId(): UniqueEntityId {
        return aggregateId
    }

    fun getTopic(): String {
        return generateStandardizedTopic(this::class.simpleName ?: "UNKNOWN-EVENT")
    }

    val eventType: String = getTopic()

    private fun generateStandardizedTopic(rawName: String): String {
        return buildString {
            append("wofuf-")
            append(rawName.lowercase())
        }
    }
}
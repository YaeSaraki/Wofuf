package dev.saraki.wofuf.shared.domain.events

import dev.saraki.wofuf.shared.domain.UniqueEntityId
import java.time.LocalDateTime

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 21:08
 *   @description:
 */
interface IDomainEvent {
    val dataTimeOccurred: LocalDateTime
    fun getAggregateId(): UniqueEntityId
}
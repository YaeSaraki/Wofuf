package dev.saraki.wofuf.shared.domain

import java.util.*

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 21:09
 *   @description:
 */
class UniqueEntityId(
    val uuid: UUID? = UUID.randomUUID()
) {
    constructor(uuid: String) : this(UUID.fromString(uuid))
    constructor() : this(UUID.randomUUID())
}
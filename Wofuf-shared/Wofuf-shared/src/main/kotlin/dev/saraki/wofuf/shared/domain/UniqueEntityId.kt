package dev.saraki.wofuf.shared.domain

import dev.saraki.wofuf.shared.utils.Uuid7Util
import java.util.*

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 21:09
 *   @description:
 */
class UniqueEntityId(
    val uuid: UUID? = Uuid7Util.generate()
) {
    constructor(uuid: String) : this(UUID.fromString(uuid))
    constructor() : this(UUID.randomUUID())
}
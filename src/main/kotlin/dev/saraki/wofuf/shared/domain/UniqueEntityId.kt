package dev.saraki.wofuf.shared.domain

import java.util.UUID

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 21:09
 *   @description:
 */
class UniqueEntityId {
    val id: String
    val uuid: UUID

    constructor(id: String) {
        this.id = id
        this.uuid = UUID.fromString(id)
    }
    constructor(id: UUID) {
        this.uuid = id
        this.id = id.toString()
    }
    constructor() {
        this.uuid = UUID.randomUUID()
        this.id = this.uuid.toString()
    }
}
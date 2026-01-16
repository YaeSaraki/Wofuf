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

    constructor(id: String) {
        this.id = id
    }
    constructor() {
        this.id = UUID.randomUUID().toString()
    }
}
package dev.saraki.wofuf.modules.users.domain

import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import dev.saraki.wofuf.shared.domain.ValueObject
import java.util.UUID

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 23:16
 *   @description: 用户身份值对象
 */
class UserId(val value: UniqueEntityId) : ValueObject<UserId>() {

        companion object {
            fun create(): Result<UserId> {
                return Result.success(UserId(UniqueEntityId()))
            }
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as UserId
        return value == other.value
    }
    override fun hashCode(): Int = value.hashCode()
    override fun toString(): String = value.toString()
}
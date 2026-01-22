package dev.saraki.wofuf.modules.players.domain

import dev.saraki.wofuf.shared.domain.ValueObject
import kotlin.io.encoding.Base64

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/2 15:44
 *   @description:
 */
data class PlayerSkin (
    val type: String,
    val skin: String,
    val cape: String,
): ValueObject<PlayerSkin>() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PlayerSkin
        return type == other.type && skin == other.skin && cape == other.cape
    }

    override fun hashCode(): Int {
        return type.hashCode() * 31 + skin.hashCode() * 31 + cape.hashCode() * 31
    }

    override fun toString(): String {
        return "PlayerSkin(type='$type', skin=$skin, cape=$cape)"
    }
}

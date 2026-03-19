package dev.saraki.wofuf.modules.players.domain.valueObjects

import dev.saraki.wofuf.shared.domain.ValueObject

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/2 15:44
 *   @description:
 */
data class PlayerSkinProps(
    val type: String,
    val skin: String,
    val cape: String,
)

class PlayerSkin private constructor(
    props: PlayerSkinProps
) : ValueObject<PlayerSkinProps>(props) {

    val type: String
        get() = props.type

    val skin: String
        get() = props.skin

    val cape: String
        get() = props.cape

    companion object {
        fun create(
            type: String,
            skin: String,
            cape: String,
        ): Result<PlayerSkin> {
            return Result.success(
                PlayerSkin(
                    PlayerSkinProps(
                        type = type,
                        skin = skin,
                        cape = cape,
                    )
                )
            )
        }

        val defaultProps = PlayerSkin(
            PlayerSkinProps(
                type = "default",
                skin = "",
                cape = "",
            )
        )
    }
}

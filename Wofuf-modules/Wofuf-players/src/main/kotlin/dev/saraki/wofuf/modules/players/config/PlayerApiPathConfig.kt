package dev.saraki.wofuf.modules.players.config

import dev.saraki.wofuf.shared.config.ApiConstantV1

object PlayerApiConstantV1 {
    private const val BASE = "${ApiConstantV1.API_BASE_PATH}/players"

    // ===================== 路径参数 =====================
    object Param {
        const val PLAYER_UUID = "playerUuid"
        const val PLAYER_NAME_OR_UUID = "playerNameOrUuid"
    }

    // ===================== 基础路径 =====================
    object Base {
        /** /api/v1/players */
        const val ROOT = BASE

        /** /api/v1/players/{playerUuid} */
        const val BY_UUID = "$BASE/{${Param.PLAYER_UUID}}"

        /** /api/v1/players/playerNameOrUuid/{playerNameOrUuid} */
        const val BY_NAME_OR_UUID = "$BASE/playerNameOrUuid/{${Param.PLAYER_NAME_OR_UUID}}"
    }

    // ===================== 玩家数据路径 =====================
    object Data {
        /** /api/v1/players/advancements/{playerUuid} */
        const val ADVANCEMENTS = "$BASE/advancements/{${Param.PLAYER_UUID}}"

        /** /api/v1/players/statistics/{playerUuid} */
        const val STATISTICS = "$BASE/statistics/{${Param.PLAYER_UUID}}"

        /** /api/v1/players/skins/{playerUuid} */
        const val SKINS = "$BASE/skins/{${Param.PLAYER_UUID}}"
    }

    // ===================== 特殊功能路径 =====================
    object Features {
        /** /api/v1/players/random-profile */
        const val RANDOM_PROFILE = "$BASE/random-profile"

        /** /api/v1/players/yesterday */
        const val YESTERDAY_ONLINE = "$BASE/yesterday"

        /** /api/v1/players/search */
        const val SEARCH = "$BASE/search"

        /** /api/v1/players/server-status */
        const val SERVER_STATUS = "$BASE/server-status"
    }
}
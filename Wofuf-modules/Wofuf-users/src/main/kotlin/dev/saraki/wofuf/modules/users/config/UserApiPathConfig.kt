package dev.saraki.wofuf.modules.users.config

import dev.saraki.wofuf.shared.config.ApiConstantV1

object UserApiConstantV1 {
    private const val BASE = "${ApiConstantV1.API_BASE_PATH}/users"

    // ===================== 路径参数 =====================
    object Param {
        const val USER_ID = "userId"
        const val USERNAME = "username"
    }

    // ===================== 基础路径 =====================
    object Base {
        /** /api/v1/users */
        const val ROOT = BASE

        /** /api/v1/users/{userId} */
        const val BY_ID = "$BASE/{${Param.USER_ID}}"

        /** /api/v1/users/username/{username} */
        const val BY_USERNAME = "$BASE/username/{${Param.USERNAME}}"

        /** /api/v1/users/me */
        const val ME = "$BASE/me"
    }

    // ===================== 当前用户相关路径 =====================
    object Me {
        /** /api/v1/users/me */
        const val PROFILE = Base.ME

        /** /api/v1/users/me/sessions */
        const val SESSIONS = "${Base.ME}/sessions"

        /** /api/v1/users/me/tokens */
        const val TOKENS = "${Base.ME}/tokens"

        /** /api/v1/users/me/settings */
        const val SETTINGS = "${Base.ME}/settings"

        /** /api/v1/users/me/activities */
        const val ACTIVITIES = "${Base.ME}/activities"
    }

    // ===================== 用户管理路径（管理员） =====================
    object Admin {
        /** /api/v1/users */
        const val LIST = Base.ROOT

        /** /api/v1/users/{userId} */
        const val BY_ID = Base.BY_ID

        /** /api/v1/users/{userId}/status */
        const val STATUS = "${Base.BY_ID}/status"

        /** /api/v1/users/{userId}/roles */
        const val ROLES = "${Base.BY_ID}/roles"

        /** /api/v1/users/{userId}/permissions */
        const val PERMISSIONS = "${Base.BY_ID}/permissions"
    }

    // ===================== 用户公开信息路径 =====================
    object Public {
        /** /api/v1/users/username/{username}/profile */
        const val PROFILE_BY_USERNAME = "${Base.BY_USERNAME}/profile"

        /** /api/v1/users/{userId}/profile */
        const val PROFILE_BY_ID = "${Base.BY_ID}/profile"
    }
}
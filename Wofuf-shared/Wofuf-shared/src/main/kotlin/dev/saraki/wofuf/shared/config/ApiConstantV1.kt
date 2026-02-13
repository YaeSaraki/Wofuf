package dev.saraki.wofuf.shared.config

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/25 15:24
 *   @description:
 */
import org.springframework.stereotype.Component

@Component
object ApiConstantV1 {
    const val API_VERSION = "v1"
    const val API_BASE_PATH = "/api/$API_VERSION"

    const val HEALTH_PATH = "$API_BASE_PATH/health"
    const val SWAGGER_UI_PATH = "/swagger-ui/index.html"
}
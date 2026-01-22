package dev.saraki.wofuf.config

import dev.saraki.wofuf.modules.users.services.auth.AuthProperties
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/21 23:29
 *   @description:
 */
@Configuration
@EnableScheduling
@EnableConfigurationProperties(AuthProperties::class)
class AppConfig(
    @Value("\${app.is-production}")
    val isProduction: Boolean
    ,
    @Value("\${app.approved-domain-list}")
    val approvedDomainList: List<String>
)

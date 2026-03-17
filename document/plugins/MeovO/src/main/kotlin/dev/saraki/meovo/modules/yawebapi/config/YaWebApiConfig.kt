package dev.saraki.meovo.modules.yawebapi.config

import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigFile

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/11 21:09
 *   @description:
 */
object YaWebApiConfig {
    @Config("config/YaWebApi/webserver.yml", autoReload = true)
    lateinit var config: ConfigFile

    val getTargetServerHost: String
        get() = config.getString("target-server-host") ?: "127.0.0.1"

    val getTargetServerPort: Int
        get() = config.getInt("target-server-port", 8080)

    val getAllowedOrigins: List<String>
        get() = config.getStringList("allowedOrigins")

     val getAllowedIpAddresses: List<String>
        get() = config.getStringList("allowedIpAddresses")

    val getSecretKey: String
        get() = config.getString("secret-key") ?: "这个密钥非常非常非常非常非常非常非常非常安全"
}

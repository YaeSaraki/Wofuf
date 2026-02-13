package dev.saraki.wofuf.modules.forum

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/24 23:07
 *   @description:
 */
@SpringBootApplication(
    scanBasePackages = [
        "dev.saraki.wofuf.auth",
        "dev.saraki.wofuf.shared",
        "dev.saraki.wofuf.modules.forum"
    ]
)
class ForumApplication

fun main(args: Array<String>) {
    runApplication<ForumApplication>(*args)
}

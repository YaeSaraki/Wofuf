package dev.saraki.wofuf.modules.forum

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.persistence.autoconfigure.EntityScan
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
@EntityScan(
    basePackages = [
        "dev.saraki.wofuf.modules.forum.infra.repos.jpa.entities",
        "dev.saraki.wofuf.modules.players.infra.repos.jpa.entities"
    ]
)
class ForumApplication

fun main(args: Array<String>) {
    runApplication<ForumApplication>(*args)
}

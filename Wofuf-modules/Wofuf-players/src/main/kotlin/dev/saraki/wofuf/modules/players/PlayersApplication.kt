package dev.saraki.wofuf.modules.players

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * Players模块应用程序入口
 */
@EnableScheduling // 开启定时任务核心注解
@SpringBootApplication(
    scanBasePackages = [
        "dev.saraki.wofuf.modules.players",
        "dev.saraki.wofuf.shared"
    ]
)
class PlayersApplication

fun main(args: Array<String>) {
    runApplication<PlayersApplication>(*args)
}
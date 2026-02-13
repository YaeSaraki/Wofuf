package dev.saraki.wofuf

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling // 开启定时任务核心注解
@SpringBootApplication(
    scanBasePackages = [
        "dev.saraki.wofuf.modules.users",
        "dev.saraki.wofuf.modules.players",
        "dev.saraki.wofuf.modules.forum",
    ]
)
class WofufApplication

/* 单体 */
fun main(args: Array<String>) {
    runApplication<WofufApplication>(*args)
}
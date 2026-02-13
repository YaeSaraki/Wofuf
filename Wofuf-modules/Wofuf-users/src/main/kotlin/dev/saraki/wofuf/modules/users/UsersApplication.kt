package dev.saraki.wofuf.modules.users

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Players模块应用程序入口
 */
@SpringBootApplication(
    scanBasePackages = [
        "dev.saraki.wofuf.auth",
        "dev.saraki.wofuf.modules.users",
        "dev.saraki.wofuf.shared"
    ]
)
class UsersApplication

fun main(args: Array<String>) {
    runApplication<UsersApplication>(*args)
}
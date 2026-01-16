package dev.saraki.wofuf

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Configuration

@SpringBootApplication
class WoffoApplication

fun main(args: Array<String>) {
    runApplication<WoffoApplication>(*args)
}

@Configuration
class AppConfig(
    @Value("\${app.is-production}")
    val isProduction: Boolean
    ,
    @Value("\${app.approved-domain-list}")
    val approvedDomainList: List<String>
) {
}

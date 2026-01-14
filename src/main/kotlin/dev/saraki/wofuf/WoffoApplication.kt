package dev.saraki.wofuf

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class WoffoApplication

fun main(args: Array<String>) {
    runApplication<WoffoApplication>(*args)
}

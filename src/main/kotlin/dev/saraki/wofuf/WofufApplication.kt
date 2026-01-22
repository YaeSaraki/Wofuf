package dev.saraki.wofuf

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class WofufApplication

fun main(args: Array<String>) {
    runApplication<WofufApplication>(*args)
}
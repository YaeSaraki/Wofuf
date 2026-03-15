// settings.gradle.kts
rootProject.name = "Wofuf"

// 定义子模块
include(
    "shared",
    "shared-auth",
    "infra-discovery",
    "infra-gateway",
    "modules-users",
    "modules-players",
    "modules-forum"
)

project(":shared").projectDir = file("Wofuf-shared/Wofuf-shared")
project(":shared-auth").projectDir = file("Wofuf-shared/Wofuf-auth")
project(":infra-discovery").projectDir = file("Wofuf-infra/Wofuf-discovery")
project(":infra-gateway").projectDir = file("Wofuf-infra/Wofuf-gateway")
project(":modules-users").projectDir = file("Wofuf-modules/Wofuf-users")
project(":modules-players").projectDir = file("Wofuf-modules/Wofuf-players")
project(":modules-forum").projectDir = file("Wofuf-modules/Wofuf-forum")

pluginManagement {
    plugins {
        id("org.jetbrains.kotlin.jvm") version "2.2.21"
        id("org.jetbrains.kotlin.plugin.spring") version "2.2.21"
        id("org.jetbrains.kotlin.plugin.noarg") version "2.1.21"
        id("org.jetbrains.kotlin.plugin.allopen") version "2.1.21"
        id("org.springframework.boot") version "4.0.1"
        id("io.spring.dependency-management") version "1.1.7"
        id("org.asciidoctor.jvm.convert") version "4.0.5"
    }
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

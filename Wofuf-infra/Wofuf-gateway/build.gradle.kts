plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "dev.saraki"
version = "0.0.1-SNAPSHOT"
description = "Wofuf Gateway"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Cloud Gateway
    implementation("org.springframework.cloud:spring-cloud-starter-gateway-server-webflux")

    // 服务发现
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")

    // 负载均衡
    implementation("org.springframework.cloud:spring-cloud-starter-loadbalancer")

    // 安全（可选）
    // implementation("org.springframework.boot:spring-boot-starter-security")

    // 测试
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:${libs.versions.spring.boot.get()}")
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${libs.versions.spring.cloud.get()}")
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
                "-Xjsr305=strict",
                "-Xannotation-default-target=param-property"
        )
    }
}

tasks.named("build") {
    dependsOn("jar")
}

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.spring")
    id("io.spring.dependency-management")
}

group = "dev.saraki"
version = "0.0.1-SNAPSHOT"
description = "Wofuf Auth"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:${libs.versions.spring.boot.get()}")
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${libs.versions.spring.cloud.get()}")
    }
}

dependencies {
    // 服务发现
    implementation(libs.spring.cloud.eureka)

    // 负载均衡
    implementation(libs.spring.cloud.loadbalancer)

    // 安全
    implementation(libs.spring.boot.starter.security)

    // Web
    implementation(libs.spring.boot.starter.web)

    // Redis
    implementation(libs.spring.boot.starter.data.redis)

    // 测试依赖
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.restdocs.mockmvc)    // MyBatis

    implementation(libs.mybatis.starter)
    testImplementation(libs.mybatis.starter.test)

    // Spring Cloud Stream + Kafka
    implementation(libs.spring.cloud.kafka)
    implementation(libs.spring.cloud.starter.stream.kafka)

    // jjwt
    implementation(libs.jjwt.api)
    runtimeOnly(libs.jjwt.impl)
    runtimeOnly(libs.jjwt.jackson)

    // Gson
    implementation(libs.gson)

    // UUID
    implementation(libs.uuid)

    // Kotlin logging
    implementation(libs.kotlin.logging)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xjsr305=strict",
            "-Xannotation-default-target=param-property"
        )
    }
}
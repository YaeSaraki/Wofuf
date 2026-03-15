plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.spring")
    id("org.jetbrains.kotlin.plugin.noarg")
    id("org.jetbrains.kotlin.plugin.allopen")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("org.asciidoctor.jvm.convert")
}

group = "dev.saraki"
version = "0.0.1-SNAPSHOT"
description = "Wofuf Forum"

noArg {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.Embeddable")
    annotation("jakarta.persistence.MappedSuperclass")
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
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
    implementation(project(":shared"))
    implementation(project(":shared-auth"))
    implementation(project(":modules-users"))
    implementation(project(":modules-players"))

    // 基础依赖
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.apache.commons:commons-pool2")

    // Spring Boot 核心依赖
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.webmvc)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.data.redis)
    implementation(libs.spring.boot.starter.security)

    // 服务发现
    implementation(libs.spring.cloud.eureka)

    // 测试依赖
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.restdocs.mockmvc)

    // MyBatis
    implementation(libs.mybatis.starter)
    testImplementation(libs.mybatis.starter.test)

    // Spring Cloud Stream + Kafka
    implementation(libs.spring.cloud.kafka)
    implementation(libs.spring.cloud.starter.stream.kafka)

    // 配置处理器
    annotationProcessor(libs.spring.boot.configuration.processor)

    // 数据库驱动
    runtimeOnly(libs.mysql.connector.java)

    // Lombok
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    // Kotlin logging
    implementation(libs.kotlin.logging)

    // jjwt
    implementation(libs.jjwt.api)
    runtimeOnly(libs.jjwt.impl)
    runtimeOnly(libs.jjwt.jackson)

    // Gson
    implementation(libs.gson)

    // UUID
    implementation(libs.uuid)

    // SpringDoc OpenAPI
    implementation(libs.springdoc.openapi.starter.webmvc.ui)
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

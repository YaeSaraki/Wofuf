plugins {
    java
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("org.jetbrains.kotlin.jvm")
    id ("org.jetbrains.kotlin.plugin.noarg")
    id ("org.jetbrains.kotlin.plugin.allopen")
    id("org.jetbrains.kotlin.plugin.spring")
    id("org.asciidoctor.jvm.convert")
}

group = "dev.saraki"
version = "0.0.1-SNAPSHOT"

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
    runtimeOnly(libs.jjwt.impl)
    runtimeOnly(libs.jjwt.jackson)
    runtimeOnly("com.mysql:mysql-connector-j")

    implementation(project(":shared"))
    implementation(project(":shared-auth"))
    implementation(project(":modules-users"))
    implementation(project(":modules-players"))
    implementation(project(":modules-forum"))

    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.data.redis)
    implementation(libs.spring.boot.starter.security)

    implementation(libs.spring.cloud.eureka)
    implementation(libs.spring.cloud.loadbalancer)
    implementation(libs.spring.cloud.kafka)

    implementation(libs.mybatis.starter)
    testImplementation(libs.mybatis.starter.test)

    implementation(libs.jjwt.api)

    implementation(libs.gson)
    implementation(libs.uuid)
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.logging)
}

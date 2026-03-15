plugins {
    id("org.springframework.boot") version "4.0.1"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.jetbrains.kotlin.jvm") version "2.2.21"
    id ("org.jetbrains.kotlin.plugin.noarg") version "2.1.21"
    id ("org.jetbrains.kotlin.plugin.allopen") version "2.1.21"
    id("org.jetbrains.kotlin.plugin.spring") version "2.2.21"
    id("org.asciidoctor.jvm.convert") version "4.0.5"
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
    runtimeOnly(libs.jjwt.impl)
    runtimeOnly(libs.jjwt.jackson)

    implementation(libs.gson)
    implementation(libs.uuid)
    implementation(libs.kotlin.logging)
    runtimeOnly("com.mysql:mysql-connector-j")
}

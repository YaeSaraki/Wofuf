package dev.saraki.wofuf

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate

@SpringBootApplication
class WofufApplication

fun main(args: Array<String>) {
    runApplication<WofufApplication>(*args)
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

/**
 * 最简配置：仅注册 RedisTemplate Bean，不额外配置序列化器
 * 确保：
 * 1. 类上添加 @Configuration 注解（Spring 识别配置类的标记）
 * 2. 包路径在主启动类扫描范围内（dev.saraki.wofuf.config 符合要求）
 * 3. 方法参数 RedisConnectionFactory 由 Spring 自动注入
 */
@Configuration
class RedisConfig {

    // ************************ 核心：注册 RedisTemplate 类型 Bean ************************
    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
        // 初始化 RedisTemplate 对象
        val redisTemplate = RedisTemplate<String, Any>()

        // 绑定 Redis 连接工厂（必须设置，否则 RedisTemplate 无法工作）
        redisTemplate.connectionFactory = connectionFactory

        // 无需额外配置，先确保 Bean 注册成功
        return redisTemplate
    }
}
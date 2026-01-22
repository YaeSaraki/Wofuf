package dev.saraki.wofuf.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/21 23:30
 *   @description:
 */
@Configuration
class RedisConfig {

    @Bean
    fun redisObjectMapper(): ObjectMapper =
        ObjectMapper().registerKotlinModule()

    @Bean
    fun redisTemplate(
        connectionFactory: RedisConnectionFactory,
    ): RedisTemplate<String, Any> {
        val template = RedisTemplate<String, Any>()
        template.connectionFactory = connectionFactory
        val keySerializer = StringRedisSerializer()
        template.keySerializer = keySerializer
        template.hashKeySerializer = keySerializer

        template.afterPropertiesSet()
        return template
    }
}
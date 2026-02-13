package dev.saraki.wofuf.auth.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisPassword
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "auth.redis")
data class AuthRedisConfig (
    var host: String = "localhost",
    var port: Int = 6379,
    var username: String = "",
    var password: String = "",
    var database: Int = 0
) {

    @Bean("AuthRedisConnectionFactory")
    fun connectionFactory(): RedisConnectionFactory {
        val config = RedisStandaloneConfiguration()
        config.hostName = host
        config.port = port
        config.username = username
        config.password = RedisPassword.of(password)
        config.database = database
        return LettuceConnectionFactory(config)
    }

    @Bean("AuthRedisTemplate")
    fun redisTemplate(
        @Qualifier("AuthRedisConnectionFactory")
        connectionFactory: RedisConnectionFactory
    ): RedisTemplate<String, Any> {
        val template = RedisTemplate<String, Any>()
        template.connectionFactory = connectionFactory

        val jsonSerializer = RedisSerializer.json()

        template.keySerializer = StringRedisSerializer()
        template.valueSerializer = jsonSerializer
        template.hashKeySerializer = StringRedisSerializer()
        template.hashValueSerializer = jsonSerializer

        template.afterPropertiesSet()
        return template
    }
}
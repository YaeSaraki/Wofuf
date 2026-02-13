package dev.saraki.wofuf.modules.players.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisPassword
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
@ConfigurationProperties(prefix = "players.redis")
class PlayerRedisConfig {

    var host: String = "localhost"
    var port: Int = 6379
    var username: String = ""
    var password: String = ""
    var database: Int = 0

    @Bean("PlayerRedisConnectionFactory")
    fun connectionFactory(): RedisConnectionFactory {
        val config = RedisStandaloneConfiguration().apply {
            hostName = this@PlayerRedisConfig.host
            port = this@PlayerRedisConfig.port
            username = this@PlayerRedisConfig.username
            password = RedisPassword.of(this@PlayerRedisConfig.password)
            database = this@PlayerRedisConfig.database
        }
        return LettuceConnectionFactory(config)
    }

    @Bean("PlayerRedisTemplate")
    @Primary
    fun playerRedisTemplate(
        @Qualifier("PlayerRedisConnectionFactory")
        connectionFactory: RedisConnectionFactory
    ): RedisTemplate<String, Any> {
        return createRedisTemplate(connectionFactory)
    }

    @Bean("redisTemplate")
    fun redisTemplate(
        @Qualifier("PlayerRedisConnectionFactory")
        connectionFactory: RedisConnectionFactory
    ): RedisTemplate<String, Any> {
        return createRedisTemplate(connectionFactory)
    }

    private fun createRedisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
        return RedisTemplate<String, Any>().apply {
            this.connectionFactory = connectionFactory

            val jsonSerializer = RedisSerializer.json()

            keySerializer = StringRedisSerializer()
            valueSerializer = jsonSerializer
            hashKeySerializer = StringRedisSerializer()
            hashValueSerializer = jsonSerializer

            afterPropertiesSet()
        }
    }
}
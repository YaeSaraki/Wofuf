# Configuration Patterns

This document describes the configuration patterns used in the Wofuf project for different infrastructure components.

## API Path Configuration

### Module API Constants Pattern
```kotlin
package dev.saraki.wofuf.modules.players.config

import dev.saraki.wofuf.shared.config.ApiConstantV1

object PlayerApiConstantV1 {
    private const val BASE = "${ApiConstantV1.API_BASE_PATH}/players"

    object Param {
        const val PLAYER_UUID = "playerUuid"
        const val PLAYER_NAME_OR_UUID = "playerNameOrUuid"
    }

    object Base {
        const val ROOT = BASE
        const val BY_UUID = "$BASE/{${Param.PLAYER_UUID}}"
        const val BY_NAME_OR_UUID = "$BASE/playerNameOrUuid/{${Param.PLAYER_NAME_OR_UUID}}"
    }
}
```

### Forum API Configuration
```kotlin
package dev.saraki.wofuf.modules.forum.config

import dev.saraki.wofuf.shared.config.ApiConstantV1

object ForumApiConstantV1 {
    private const val BASE = "${ApiConstantV1.API_BASE_PATH}/forum"

    object Param {
        const val POST_ID = "postId"
        const val COMMENT_ID = "commentId"
        const val MEMBER_ID = "memberId"
    }

    object Base {
        const val ROOT = BASE
    }

    object Members {
        const val ROOT = "$BASE/members"
        const val BY_ID = "$ROOT/{${Param.MEMBER_ID}}"
    }

    object Posts {
        const val ROOT = "$BASE/posts"
        const val BY_ID = "$ROOT/{${Param.POST_ID}}"
        const val LIKES = "$BY_ID/likes"
        const val COMMENTS = "$BY_ID/comments"
    }

    object Comments {
        const val ROOT = "$BASE/comments"
        const val BY_ID = "$ROOT/{${Param.COMMENT_ID}}"
        const val BY_POST_SLUG = "$ROOT/post/{postSlug}"
        const val REPLIES = "$BY_ID/replies"
    }

    // Utility methods
    fun buildPostPath(postId: String): String = Posts.BY_ID.replace("{${Param.POST_ID}}", postId)
    fun buildCommentPath(commentId: String): String = Comments.BY_ID.replace("{${Param.COMMENT_ID}}", commentId)
    fun buildCommentsByPostSlugPath(postSlug: String): String = Comments.BY_POST_SLUG.replace("{postSlug}", postSlug)
    fun buildCommentRepliesPath(commentId: String): String = Comments.REPLIES.replace("{${Param.COMMENT_ID}}", commentId)
}
```

## Shared API Configuration

### Base API Constants
```kotlin
package dev.saraki.wofuf.shared.config

object ApiConstantV1 {
    const val API_BASE_PATH = "/api/v1"
    const val API_VERSION = "v1"

    object Headers {
        const val AUTHORIZATION = "Authorization"
        const val CONTENT_TYPE = "Content-Type"
        const val ACCEPT = "Accept"
        const val USER_AGENT = "User-Agent"
    }

    object MediaTypes {
        const val JSON = "application/json"
        const val JSON_UTF8 = "application/json;charset=UTF-8"
        const val FORM_DATA = "multipart/form-data"
    }
}
```

## Database Configuration

### JPA Configuration
```kotlin
package dev.saraki.wofuf.modules.players.config

import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.annotation.EnableTransactionManagement
import javax.persistence.EntityManagerFactory
import javax.sql.DataSource

@Configuration
@EnableJpaRepositories(
    basePackages = ["dev.saraki.wofuf.modules.players.infra.repos.jpa"]
)
@EnableTransactionManagement
class JpaConfig {

    @Bean
    fun entityManagerFactory(
        dataSource: DataSource,
        jpaProperties: JpaProperties
    ): LocalContainerEntityManagerFactoryBean {
        return LocalContainerEntityManagerFactoryBean().apply {
            this.dataSource = dataSource
            setPackagesToScan("dev.saraki.wofuf.modules.players.infra.repos.jpa.entities")
            jpaVendorAdapter = HibernateJpaVendorAdapter()
            setJpaProperties(jpaProperties.properties)
        }
    }

    @Bean
    fun transactionManager(entityManagerFactory: EntityManagerFactory): JpaTransactionManager {
        return JpaTransactionManager(entityManagerFactory)
    }
}
```

### DataSource Configuration
```kotlin
package dev.saraki.wofuf.modules.players.config

import com.zaxxer.hikari.HikariDataSource
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import javax.sql.DataSource

@Configuration
class DataSourceConfig {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.players")
    fun playersDataSource(): DataSource {
        return HikariDataSource().apply {
            poolName = "PlayersHikariPool"
            maximumPoolSize = 20
            minimumIdle = 5
            connectionTimeout = 20000
            idleTimeout = 300000
            maxLifetime = 1200000
        }
    }
}
```

## Eventuate Tram Configuration

### Event Publisher Configuration
```kotlin
package dev.saraki.wofuf.shared.infra.events

import io.eventuate.tram.events.publisher.DomainEventPublisher
import io.eventuate.tram.events.publisher.TramEventsPublisherConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(TramEventsPublisherConfiguration::class)
class EventuateTramConfig {

    @Bean
    fun domainEventPublisher(): DomainEventPublisher {
        return EventuateTramDomainEventPublisher()
    }
}
```

### Event Subscriber Configuration
```kotlin
package dev.saraki.wofuf.shared.infra.events

import io.eventuate.tram.events.subscriber.DomainEventDispatcher
import io.eventuate.tram.events.subscriber.DomainEventDispatcherFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class EventSubscriberConfig {

    @Bean
    fun domainEventDispatcher(
        domainEventDispatcherFactory: DomainEventDispatcherFactory,
        eventHandlers: List<Any>
    ): DomainEventDispatcher {
        return domainEventDispatcherFactory.make(
            "eventDispatcher",
            eventHandlers
        )
    }
}
```

## Security Configuration

### JWT Configuration
```kotlin
package dev.saraki.wofuf.modules.users.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "auth.jwt")
class JwtConfig {
    lateinit var secret: String
    var expiration: Long = 86400000 // 24 hours
    var refreshExpiration: Long = 604800000 // 7 days

    fun getExpirationDate(): Date {
        return Date(System.currentTimeMillis() + expiration)
    }

    fun getRefreshExpirationDate(): Date {
        return Date(System.currentTimeMillis() + refreshExpiration)
    }
}
```

### Spring Security Configuration
```kotlin
package dev.saraki.wofuf.modules.users.config

import dev.saraki.wofuf.modules.users.infra.security.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http.csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
            .antMatchers("/api/v1/auth/**").permitAll()
            .antMatchers("/api/v1/players/random").permitAll()
            .anyRequest().authenticated()
            .and()
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}
```

## Caching Configuration

### Redis Configuration
```kotlin
package dev.saraki.wofuf.shared.config

import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import java.time.Duration

@Configuration
@EnableCaching
class CacheConfig {

    @Bean
    fun cacheManager(redisConnectionFactory: RedisConnectionFactory): RedisCacheManager {
        val config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1))
            .disableCachingNullValues()

        return RedisCacheManager.builder(redisConnectionFactory)
            .cacheDefaults(config)
            .build()
    }
}
```

### Cache-Specific Configuration
```kotlin
package dev.saraki.wofuf.modules.players.config

import org.springframework.cache.annotation.CacheConfig
import org.springframework.context.annotation.Configuration

@Configuration
@CacheConfig(cacheNames = ["players", "playerStats"])
class PlayerCacheConfig
```

## Validation Configuration

### Bean Validation Configuration
```kotlin
package dev.saraki.wofuf.shared.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import javax.validation.Validator

@Configuration
class ValidationConfig {

    @Bean
    fun validator(): Validator {
        return LocalValidatorFactoryBean()
    }
}
```

## Application Properties

### Main Application Configuration
```yaml
# src/main/resources/application.yml
spring:
  profiles:
    active: dev

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: true

  datasource:
    players:
      url: jdbc:mysql://localhost:3307/woffo_db?allowPublicKeyRetrieval=true&useSSL=false
      username: woffo_db_user
      password: password
      driver-class-name: com.mysql.cj.jdbc.Driver

logging:
  level:
    dev.saraki.wofuf: INFO
    org.springframework.security: DEBUG
    io.eventuate: DEBUG

auth:
  jwt:
    secret: ${JWT_SECRET:my-super-secret-key-change-in-production}
    expiration: 86400000
    refresh-expiration: 604800000

eventuate:
  tram:
    db:
      driver: com.mysql.cj.jdbc.Driver
      url: jdbc:mysql://localhost:3307/woffo_db
      username: woffo_db_user
      password: password
    events:
      duplicate:
        detection:
          enabled: true
```

### Profile-Specific Configuration
```yaml
# src/main/resources/application-dev.yml
spring:
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

logging:
  level:
    dev.saraki.wofuf: DEBUG
    org.springframework: INFO
```

```yaml
# src/main/resources/application-prod.yml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

logging:
  level:
    dev.saraki.wofuf: WARN
    org.springframework: WARN
```

## Testing Configuration

### Test Database Configuration
```kotlin
package dev.saraki.wofuf.shared.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.junit.jupiter.Container
import javax.sql.DataSource

@TestConfiguration
class TestDatabaseConfig {

    companion object {
        @Container
        val mysqlContainer = MySQLContainer<Nothing>("mysql:8.0").apply {
            withDatabaseName("testdb")
            withUsername("test")
            withPassword("test")
        }
    }

    @Bean
    @Primary
    fun testDataSource(): DataSource {
        mysqlContainer.start()
        return mysqlContainer.createConnection("")
    }
}
```

### Test Event Configuration
```kotlin
package dev.saraki.wofuf.shared.infra.events

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@TestConfiguration
class TestEventConfig {

    @Bean
    @Primary
    fun testDomainEventPublisher(): DomainEventPublisher {
        return TestDomainEventPublisher()
    }
}

class TestDomainEventPublisher : DomainEventPublisher {
    val publishedEvents = mutableListOf<IDomainEvent>()

    override fun publish(event: IDomainEvent) {
        publishedEvents.add(event)
    }

    override fun publishAll(events: List<IDomainEvent>) {
        publishedEvents.addAll(events)
    }

    fun clear() {
        publishedEvents.clear()
    }
}
```

## Docker Configuration

### Dockerfile
```dockerfile
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY src src

RUN ./gradlew build --no-daemon -x test

EXPOSE 8080

CMD ["java", "-jar", "build/libs/app.jar"]
```

### Docker Compose
```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: woffo_db
      MYSQL_USER: woffo_db_user
      MYSQL_PASSWORD: password
    ports:
      - "3307:3306"
    volumes:
      - mysql_data:/var/lib/mysql

  redis:
    image: redis:7-alpine
    ports:
      - "6380:6379"
    volumes:
      - redis_data:/data

  kafka:
    image: confluentinc/cp-kafka:7.0.0
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092,PLAINTEXT_HOST://localhost:9092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
    ports:
      - "9092:9092"
    depends_on:
      - zookeeper

  zookeeper:
    image: confluentinc/cp-zookeeper:7.0.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000

volumes:
  mysql_data:
  redis_data:
```

Configuration patterns provide:
- **Centralized Settings**: All configuration in one place
- **Environment Flexibility**: Different configs for different environments
- **Type Safety**: Strongly typed configuration classes
- **Testability**: Easy to mock and test configuration
- **Maintainability**: Clear separation of concerns</content>
<parameter name="filePath">/Users/saraki/Documents/project/Wofuf/document/agents/Infrastructure/Config.md

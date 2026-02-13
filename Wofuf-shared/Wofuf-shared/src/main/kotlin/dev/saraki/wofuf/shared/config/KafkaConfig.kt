package dev.saraki.wofuf.shared.config


import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka

@Configuration
@EnableKafka
class KafkaConfig {
    private val bootstrapServers = "localhost:9092"
}
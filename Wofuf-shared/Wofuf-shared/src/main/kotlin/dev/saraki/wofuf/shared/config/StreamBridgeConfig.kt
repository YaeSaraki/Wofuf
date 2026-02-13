package dev.saraki.wofuf.shared.config

import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder
import java.util.function.Consumer

@Configuration
class StreamBridgeConfig {

    /**
     * 事件发布器Bean
     */
    @Bean
    fun eventPublisher(streamBridge: StreamBridge): EventPublisher {
        return object : EventPublisher {
            override fun publish(event: Any, topic: String, key: String?) {
                val message: Message<Any> = MessageBuilder
                    .withPayload(event)
                    .apply { key?.let { setHeader("spring.cloud.stream.sendto.destination", topic) } }
                    .build()

                streamBridge.send(topic, message)
            }
        }
    }

    /**
     * 示例事件处理器（函数式编程模型）
     */
    @Bean
    fun orderEventProcessor(): Consumer<Message<Any>> {
        return Consumer { message ->
            val event = message.payload
            val headers = message.headers
            println("Received event: ${event.javaClass.simpleName}")
            println("Event headers: $headers")
        }
    }
}

/**
 * 事件发布器接口
 */
interface EventPublisher {
    fun publish(event: Any, topic: String, key: String? = null)
}
package dev.saraki.wofuf.shared.infra.events.kafka

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import dev.saraki.wofuf.shared.domain.AggregateRoot
import dev.saraki.wofuf.shared.domain.events.IDomainEvent
import dev.saraki.wofuf.shared.domain.events.IDomainEvents
import dev.saraki.wofuf.shared.domain.events.IDomainEventHandler
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

private const val TOPIC_PREFIX = "wofuf-"

/**
 * 核心入口组件：集成Kafka事件发布 + 消费能力
 */
@Component
class KafkaDomainEvents(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper,
    private val eventBasePackage: String = "dev.saraki.wofuf.modules"
) : IDomainEvents {

    // ===================== 常量定义 =====================
    companion object {
        private const val EVENT_TYPE_FIELD = "_eventType"  // JSON中的事件类型字段
        private const val DEFAULT_GROUP_ID = "wofuf-domain-events-group"
    }

    // ===================== 内部注册表 =====================
    private val handlers: MutableMap<String, MutableList<IDomainEventHandler<*>>> = ConcurrentHashMap()
    private val eventClassCache: MutableMap<String, Class<out IDomainEvent>> = ConcurrentHashMap()

    // 可配置的事件模块列表
    private val eventModules = listOf("users", "players", "forum")  // 根据你的项目调整

    // ===================== 事件发布 =====================
    override fun publish(domainEvent: IDomainEvent) {
        try {
            val topic = getTopicForEvent(domainEvent)
            val enhancedJson = enhanceEventJson(domainEvent)

            kafkaTemplate.send(topic, enhancedJson)
                .whenComplete { result, ex ->
                    if (ex != null) {
                        logError("Kafka发布失败", mapOf(
                            "topic" to topic,
                            "event" to domainEvent::class.simpleName,
                            "error" to ex.message
                        ))
                    } else {
                        logInfo("Kafka发布成功", mapOf(
                            "topic" to topic,
                            "event" to domainEvent::class.simpleName,
                            "offset" to result?.recordMetadata?.offset()
                        ))
                    }
                }
        } catch (e: Exception) {
            logError("Kafka发布异常", mapOf(
                "event" to domainEvent::class.simpleName,
                "error" to e.message
            ))
        }
    }

    override fun publishAll(aggregate: AggregateRoot<*>) {
        aggregate.getDomainEvents().forEach { publish(it) }
        aggregate.clearEvents()
    }

    /**
     * 增强事件JSON：添加元数据字段
     */
    private fun enhanceEventJson(event: IDomainEvent): String {
        val originalJson = objectMapper.writeValueAsString(event)
        val jsonNode = objectMapper.readTree(originalJson) as ObjectNode

        // 添加事件类型元数据
        jsonNode.put(EVENT_TYPE_FIELD, event::class.java.name)
        jsonNode.put("_timestamp", System.currentTimeMillis())
        jsonNode.put("_version", "1.0")

        return objectMapper.writeValueAsString(jsonNode)
    }

    /**
     * 获取事件对应的Topic
     */
    private fun getTopicForEvent(event: IDomainEvent): String {
        val eventName = event::class.simpleName ?: error("事件类名不能为空")
        // 将驼峰命名转为短横线命名：CreateUser -> create-user
        val topicName = eventName
            .replace(Regex("([a-z])([A-Z])"), "$1-$2")
            .lowercase()
        return "$TOPIC_PREFIX$topicName"
    }

    // ===================== 事件处理器注册 =====================
    override fun <T : IDomainEvent> subscribe(handler: IDomainEventHandler<T>) {
        val eventType = handler.getEventType().name  // 使用全限定名
        handlers.computeIfAbsent(eventType) { mutableListOf() }.add(handler)
        logInfo("事件处理器注册成功", mapOf(
            "event" to eventType,
            "handler" to handler::class.simpleName
        ))
    }

    // ===================== 事件分发 =====================
    override fun <T : IDomainEvent> handleEvent(event: T) {
        val eventType = event::class.java.name
        val matchedHandlers = handlers[eventType]

        if (matchedHandlers.isNullOrEmpty()) {
            logInfo("事件无匹配处理器", mapOf("event" to eventType))
            return
        }

        matchedHandlers.forEach { handler ->
            try {
                @Suppress("UNCHECKED_CAST")
                (handler as IDomainEventHandler<T>).handle(event)
                logInfo("事件处理成功", mapOf(
                    "event" to eventType,
                    "handler" to handler::class.simpleName
                ))
            } catch (e: Exception) {
                logError("事件处理失败", mapOf(
                    "event" to eventType,
                    "handler" to handler::class.simpleName,
                    "error" to e.message
                ))
            }
        }
    }

    // ===================== Kafka消费 =====================
    @KafkaListener(
        topicPattern = "^$TOPIC_PREFIX.*",
        groupId = DEFAULT_GROUP_ID,
        autoStartup = "true",
    )
    fun consume(
        @Payload eventJson: String,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String?
    ) {
        logInfo("收到Kafka消息", mapOf("topic" to topic))

        try {
            val jsonNode = objectMapper.readTree(eventJson)

            // 1. 从JSON中获取事件类型（优先使用全限定类名）
            val eventClassName = extractEventClassName(jsonNode, topic)
                ?: throw IllegalArgumentException("无法解析事件类型: topic=$topic")

            // 2. 获取事件类
            val eventClass = getEventClass(eventClassName)
                ?: throw IllegalArgumentException("未找到事件类: $eventClassName")

            // 3. 反序列化事件
            val event = objectMapper.readValue(eventJson, eventClass) as IDomainEvent

            // 4. 处理事件
            handleEvent(event)

            logInfo("Kafka消费成功", mapOf(
                "topic" to topic,
                "event" to eventClassName
            ))

        } catch (e: Exception) {
            logError("Kafka消费失败", mapOf(
                "topic" to topic,
                "error" to e.message
            ))
        }
    }

    /**
     * 从JSON中提取事件类名
     */
    private fun extractEventClassName(jsonNode: JsonNode, topic: String?): String? {
        // 策略1: 从元数据字段读取全限定类名（最可靠）
        jsonNode.get(EVENT_TYPE_FIELD)?.asText()?.let { return it }

        // 策略2: 从简单类名映射（兼容旧数据）
        jsonNode.get("eventType")?.asText()?.let { simpleName ->
            return mapSimpleNameToFullName(simpleName)
        }

        // 策略3: 从topic推断（兜底策略）
        topic?.let { inferClassNameFromTopic(it) }?.let { return it }

        return null
    }

    /**
     * 将简单类名映射为全限定类名
     */
    private fun mapSimpleNameToFullName(simpleName: String): String? {
        eventModules.forEach { module ->
            val fullName = "$eventBasePackage.$module.domain.events.$simpleName"
            if (isClassExists(fullName)) {
                return fullName
            }
        }
        return null
    }

    /**
     * 从topic推断类名
     */
    private fun inferClassNameFromTopic(topic: String): String? {
        val eventName = topic.removePrefix(TOPIC_PREFIX)
        // 将短横线命名转为驼峰：create-user -> CreateUser
        val className = eventName
            .split("-")
            .joinToString("") { it.replaceFirstChar { c -> c.uppercase() } }

        return mapSimpleNameToFullName(className)
    }

    /**
     * 检查类是否存在
     */
    private fun isClassExists(className: String): Boolean {
        return try {
            Class.forName(className)
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }

    /**
     * 获取事件类（带缓存）
     */
    private fun getEventClass(className: String): Class<out IDomainEvent> {
        // 先查缓存
        eventClassCache[className]?.let { return it }

        // 缓存中没有，反射加载
        return try {
            val clazz = Class.forName(className)
            require(IDomainEvent::class.java.isAssignableFrom(clazz)) {
                "类 $className 不是 IDomainEvent 的子类型"
            }
            @Suppress("UNCHECKED_CAST")
            (clazz as Class<out IDomainEvent>).also {
                eventClassCache[className] = it  // 存入缓存
            }
        } catch (e: Exception) {
            throw IllegalArgumentException("无法加载事件类: $className", e)
        }
    }

    // ===================== 日志工具 =====================
    private fun logInfo(message: String, context: Map<String, Any?>) {
        println("[INFO] $message - $context")
    }

    private fun logError(message: String, context: Map<String, Any?>) {
        println("[ERROR] $message - $context")
    }
}
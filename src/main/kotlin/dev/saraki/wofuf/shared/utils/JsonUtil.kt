package dev.saraki.wofuf.shared.utils

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * JSON 序列化/反序列化工具类
 * 适配 Kotlin、空值安全、异常友好，专为 1584 个成就的批量序列化设计
 */
object JsonUtil {
    // 初始化 ObjectMapper，适配 Kotlin 并优化序列化配置
    private val objectMapper: ObjectMapper = ObjectMapper()
        .registerKotlinModule() // 必须：支持 Kotlin 数据类、空值等特性
        .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS) // 空对象不抛异常
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) // 日期序列化为字符串

    private val log: Logger = LoggerFactory.getLogger(JsonUtil::class.java)

    /**
     * 将对象转换为JSON字符串
     * @param obj 待序列化对象（支持 null）
     * @return JSON字符串，对象为null时返回 "{}"（集合返回 "[]"）
     */
    fun <T> toJson(obj: T?): String {
        if (obj == null) {
            return when (obj) {
                is Collection<*> -> "[]"
                is Map<*, *> -> "{}"
                else -> "{}"
            }
        }
        return try {
            objectMapper.writeValueAsString(obj)
        } catch (e: Exception) {
            log.error("JSON序列化失败，对象类型：{}", obj.javaClass.name, e)
            // 序列化失败时返回空JSON，避免业务崩溃
            if (obj is Collection<*>) "[]" else "{}"
        }
    }

    /**
     * 将JSON字符串转换为对象（使用Type）
     * @param json JSON字符串（支持空/无效字符串）
     * @param type 目标类型
     * @return 反序列化后的对象，失败时返回 null
     */
    fun <T> fromJson(json: String?, type: JavaType): T? {
        if (json.isNullOrBlank() || json == "{}" || json == "[]") {
            return null
        }
        return try {
            objectMapper.readValue(json, type) as T
        } catch (e: Exception) {
            log.error("JSON反序列化失败，目标类型：{}，JSON：{}", type.typeName, json, e)
            null
        }
    }

    /**
     * 将JSON字符串转换为对象（使用TypeReference，推荐集合/泛型使用）
     * @param json JSON字符串（支持空/无效字符串）
     * @param typeReference 目标类型引用（如 Set<String>、Map<String, Long>）
     * @return 反序列化后的对象，失败时返回 null
     */
    fun <T> fromJson(json: String?, typeReference: TypeReference<T>): T? {
        if (json.isNullOrBlank() || json == "{}" || json == "[]") {
            return null
        }
        return try {
            objectMapper.readValue(json, typeReference)
        } catch (e: Exception) {
            log.error("JSON反序列化失败，目标类型：{}，JSON：{}", typeReference.type.typeName, json, e)
            null
        }
    }

    /**
     * 快捷方法：Set<String> → JSON字符串
     */
    fun setToJson(set: Set<String>?): String {
        return toJson(set ?: emptySet())
    }

    /**
     * 快捷方法：JSON字符串 → Set<String>
     */
    fun jsonToSet(json: String?): Set<String> {
        return fromJson(json, object : TypeReference<Set<String>>() {}) ?: emptySet()
    }

    /**
     * 快捷方法：Map<String, Any> → JSON字符串
     */
    fun mapToJson(map: Map<String, Any>?): String {
        return toJson(map ?: emptyMap())
    }

    /**
     * 快捷方法：JSON字符串 → Map<String, Any>
     */
    fun jsonToMap(json: String?): Map<String, Any> {
        return fromJson(json, object : TypeReference<Map<String, Any>>() {}) ?: emptyMap()
    }
}
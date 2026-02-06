package dev.saraki.wofuf.shared.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonUnwrapped

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 16:06
 *   @description:
 */
/**
 * 值对象基类
 * 核心特性：通过属性结构（而非引用）判断相等性，符合DDD值对象设计规范
 * @param T 值对象的属性类型，需为数据类（保证equals/hashCode的结构对比）
 */
abstract class ValueObject<T : Any>(
    open val props: T // 开放属性，子类通过数据类实现具体属性，对应TS的props
) {
    /**
     * 判断两个值对象是否相等
     * 核心：通过属性的JSON字符串对比实现结构相等性判断
     * @param vo 待对比的另一个值对象，可空
     * @return true=相等，false=不相等/入参为空
     */
    @JsonIgnore
    fun equals(vo: ValueObject<T>?): Boolean {
        // 处理入参为空/未定义的情况
        if (vo == null) return false

        // 核心逻辑：序列化props为JSON字符串后对比（与TS的JSON.stringify逻辑一致）
        return toJsonString(this.props) == toJsonString(vo.props)
    }

    /**
     * 辅助方法：将任意对象序列化为有序的JSON字符串
     * 解决普通JSON序列化字段顺序不一致导致的对比错误
     * @param obj 待序列化的对象（值对象的props）
     * @return 有序的JSON字符串，保证结构对比的准确性
     */
    @JsonIgnore
    private fun toJsonString(obj: Any): String {
        // 使用Jackson实现JSON序列化（Kotlin后端标准JSON库，需引入依赖）
        val objectMapper = com.fasterxml.jackson.databind.ObjectMapper()
            .configure(com.fasterxml.jackson.databind.SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
        // 若obj是数据类/Map，会按字段名排序后序列化，与TS的JSON.stringify行为对齐
        return objectMapper.writeValueAsString(obj)
    }

    @JsonIgnore
    override fun equals(other: Any?): Boolean {
        if (this === other) return true // 引用相同，直接相等
        if (javaClass != other?.javaClass) return false // 类型不同，直接不相等

        other as ValueObject<*> // 类型强转
        return equals(other) // 复用上面的equals方法
    }

    @JsonIgnore
    override fun hashCode(): Int {
        return toJsonString(props).hashCode()
    }
}
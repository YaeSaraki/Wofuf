package dev.saraki.wofuf.shared.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import com.google.gson.Gson
import com.google.gson.GsonBuilder

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 16:06
 *   @description: 值对象基类（Gson版本）
 */
/**
 * 值对象基类
 * 核心特性：通过属性结构（而非引用）判断相等性，符合DDD值对象设计规范
 * @param T 值对象的属性类型，需为数据类（保证equals/hashCode的结构对比）
 */
abstract class ValueObject<T : Any>(
    open val props: T
) {

    // 初始化Gson为单例，避免重复创建
    private val gson: Gson by lazy {
        GsonBuilder()
            .enableComplexMapKeySerialization()
            .serializeNulls()
            .setPrettyPrinting()
            .create()
    }

    /**
     * 判断两个值对象是否相等
     * 核心：通过属性的JSON字符串对比实现结构相等性判断
     * @param vo 待对比的另一个值对象，可空
     * @return true=相等，false=不相等/入参为空
     */
    @JsonIgnore
    fun equals(vo: ValueObject<T>?): Boolean {
        if (vo == null) return false
        return toString(this.props) == toString(vo.props)
    }

    /**
     * 辅助方法：将任意对象序列化为有序的JSON字符串
     */
    @JsonIgnore
    private fun toString(obj: Any): String {
        return gson.toJson(obj)
    }

    /**
     * 重写equals，保证值对象的结构相等性
     */
    @JsonIgnore
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ValueObject<*>
        return toString(this.props) == toString(other.props)
    }

    /**
     * 重写hashCode，与equals保持一致
     */
    @JsonIgnore
    override fun hashCode(): Int {
        return toString(props).hashCode()
    }
}
package dev.saraki.wofuf.shared.utils

import com.fasterxml.uuid.Generators
import java.util.UUID

/**
 * UUID 工具类，专注生成有序的 UUID v7
 * 解决原生 UUID v4 无序导致的数据库插入性能问题
 */
object Uuid7Util {
    // 初始化 UUID v7 生成器（基于时间戳，天然有序）
    private val uuid7Generator = Generators.timeBasedGenerator()

    /**
     * 生成有序的 UUID v7（推荐作为数据库主键）
     * @return 时间有序的 UUID v7 实例
     */
    fun generate(): UUID {
        return uuid7Generator.generate()
    }

    /**
     * 生成 UUID v7 的字符串形式（带横线，如：018e7f2a-8c4e-7000-8000-000000000000）
     */
    fun generateString(): String {
        return generate().toString()
    }

    /**
     * 生成无横线的 UUID v7 字符串（节省存储，如：018e7f2a8c4e70008000000000000000）
     */
    fun generateStringWithoutHyphen(): String {
        return generate().toString().replace("-", "")
    }
}
package dev.saraki.wofuf.shared.domain

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/6 13:03
 *   @description:
 */
abstract class WatchedList<T> {
    var currentItems: MutableList<T>

    // 原TS的私有属性，保持名称+访问修饰符不变
    private val initial: List<T>
    private val new: MutableList<T>
    private val removed: MutableList<T>

    /**
     * initialItems: 可选初始元素集合，默认空列表（处理TS的initialItems?）
     */
    constructor(initialItems: List<T>? = null) {
        val initItems = initialItems ?: emptyList()
        this.currentItems = initItems.toMutableList()
        this.initial = initItems // 初始集合设为不可变，保证原始值不被修改
        this.new = mutableListOf()
        this.removed = mutableListOf()
    }

    /**
     * 子类必须重写，实现自定义的元素对比逻辑（替代TS的引用对比，实现业务唯一标识对比）
     */
    abstract fun compareItems(a: T, b: T): Boolean

    // 公有方法，保留原名称，返回不可变视图（避免外部直接修改集合）
    fun getItems(): List<T> = currentItems.toList()
    fun getNewItems(): List<T> = new.toList()
    fun getRemovedItems(): List<T> = removed.toList()

    /**
     * 对应TS的filter+length !== 0逻辑，Kotlin用any更简洁高效
     */
    private fun isCurrentItem(item: T): Boolean {
        return currentItems.any { compareItems(item, it) }
    }

    /**
     * 私有方法：判断元素是否在new中
     */
    private fun isNewItem(item: T): Boolean {
        return new.any { compareItems(item, it) }
    }

    /**
     * 私有方法：判断元素是否在removed中
     */
    private fun isRemovedItem(item: T): Boolean {
        return removed.any { compareItems(item, it) }
    }

    /**
     * 私有方法：从new中移除指定元素
     */
    private fun removeFromNew(item: T) {
        new.removeIf { compareItems(it, item) }
    }

    /**
     * 私有方法：从currentItems中移除指定元素
     */
    private fun removeFromCurrent(item: T) {
        currentItems.removeIf { compareItems(it, item) }
    }

    /**
     * 私有方法：从removed中移除指定元素
     */
    private fun removeFromRemoved(item: T) {
        removed.removeIf { compareItems(it, item) }
    }

    /**
     * 私有方法：判断元素是否是初始加入的元素
     */
    private fun wasAddedInitially(item: T): Boolean {
        return initial.any { compareItems(item, it) }
    }

    /**
     * 公有方法：判断元素是否存在于当前列表
     */
    fun exists(item: T): Boolean {
        return isCurrentItem(item)
    }

    /**
     * 公有方法：添加元素，严格对齐TS的add业务逻辑
     */
    fun add(item: T) {
        // 如果元素在已移除列表，先从移除列表删除
        if (isRemovedItem(item)) {
            removeFromRemoved(item)
        }
        // 如果不是新元素且不是初始元素，加入新元素列表
        if (!isNewItem(item) && !wasAddedInitially(item)) {
            new.add(item)
        }
        // 如果当前列表没有该元素，加入当前列表
        if (!isCurrentItem(item)) {
            currentItems.add(item)
        }
    }

    /**
     * 公有方法：移除元素，严格对齐TS的remove业务逻辑
     */
    fun remove(item: T) {
        // 先从当前列表移除
        removeFromCurrent(item)

        // 如果是新添加的元素，直接从新列表移除并返回
        if (isNewItem(item)) {
            removeFromNew(item)
            return
        }

        // 如果不是已移除元素，加入移除列表
        if (!isRemovedItem(item)) {
            removed.add(item)
        }
    }
}
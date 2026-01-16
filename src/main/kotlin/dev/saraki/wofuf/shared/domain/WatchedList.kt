package dev.saraki.wofuf.shared.domain

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 21:26
 *   @description:
 */
abstract class WatchedList<T> {
    private val currentItems: MutableList<T>
    private val initial: List<T>
    private val newItems: MutableList<T>
    private val removedItems: MutableList<T>

    constructor(initialItems: List<T>? = null) {
        val items = initialItems ?: emptyList()
        this.currentItems = items.toMutableList()
        this.initial = items.toList() // 创建不可变副本
        this.newItems = mutableListOf()
        this.removedItems = mutableListOf()
    }

    abstract fun compareItems(a: T, b: T): Boolean

    fun getItems(): List<T> {
        return currentItems.toList() // 返回不可变副本
    }

    fun getNewItems(): List<T> {
        return newItems.toList() // 返回不可变副本
    }

    fun getRemovedItems(): List<T> {
        return removedItems.toList() // 返回不可变副本
    }

    private fun isCurrentItem(item: T): Boolean {
        return currentItems.any { compareItems(item, it) }
    }

    private fun isNewItem(item: T): Boolean {
        return newItems.any { compareItems(item, it) }
    }

    private fun isRemovedItem(item: T): Boolean {
        return removedItems.any { compareItems(item, it) }
    }

    private fun removeFromNew(item: T) {
        newItems.removeAll { compareItems(it, item) }
    }

    private fun removeFromCurrent(item: T) {
        currentItems.removeAll { compareItems(item, it) }
    }

    private fun removeFromRemoved(item: T) {
        removedItems.removeAll { compareItems(item, it) }
    }

    private fun wasAddedInitially(item: T): Boolean {
        return initial.any { compareItems(item, it) }
    }

    fun exists(item: T): Boolean {
        return isCurrentItem(item)
    }

    fun add(item: T) {
        if (isRemovedItem(item)) {
            removeFromRemoved(item)
        }

        if (!isNewItem(item) && !wasAddedInitially(item)) {
            newItems.add(item)
        }

        if (!isCurrentItem(item)) {
            currentItems.add(item)
        }
    }

    fun remove(item: T) {
        removeFromCurrent(item)

        if (isNewItem(item)) {
            removeFromNew(item)
            return
        }

        if (!isRemovedItem(item)) {
            removedItems.add(item)
        }
    }
}
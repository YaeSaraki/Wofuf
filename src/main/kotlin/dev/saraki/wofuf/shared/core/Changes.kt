package dev.saraki.wofuf.shared.core

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 16:45
 *   @description:
 */
class Changes {
    private val _changes: MutableList<Result<Any?>> = mutableListOf()
    public fun addChange(field: String, value: Any?) {
        _changes.add(Result.success(value))
    }
    public fun getChanges(): MutableList<Result<Any?>> {
        return _changes
    }
}
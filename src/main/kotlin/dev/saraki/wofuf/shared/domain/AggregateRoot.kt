package dev.saraki.wofuf.shared.domain

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 11:21
 *   @description:
 */
abstract class AggregateRoot<ID> {
    protected var _id: ID? = null

    fun id(): ID? {
        return _id
    }

    fun setId(id: ID) {
        this._id = id
    }
}
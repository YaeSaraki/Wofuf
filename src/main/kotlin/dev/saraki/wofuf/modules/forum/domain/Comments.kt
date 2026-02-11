package dev.saraki.wofuf.modules.forum.domain

import dev.saraki.wofuf.shared.domain.WatchedList

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/8 14:15
 *   @description:
 */
data class CommentsProps(
    val value: List<Comment>
)

class Comments private constructor(props: CommentsProps) :
    WatchedList<Comment>(props.value) {
    override fun compareItems(a: Comment, b: Comment): Boolean {
        return a == b
    }

    companion object {
        fun create(initialComments: List<Comment>? = null): Comments {
            return Comments(CommentsProps(initialComments ?: emptyList()))
        }
    }
}
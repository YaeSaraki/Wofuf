package dev.saraki.wofuf.modules.forum.domain

import dev.saraki.wofuf.shared.domain.WatchedList

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/6 13:05
 *   @description:
 */
data class CommentVotesProps(
    val value: List<CommentVote>
)

class CommentVotes private constructor(
    props: CommentVotesProps
) : WatchedList<CommentVote>(props.value) {

    override fun compareItems(a: CommentVote, b: CommentVote): Boolean {
        return a == b
    }

    companion object {
        fun create(initialVotes: List<CommentVote>? = null): CommentVotes {
            return CommentVotes(CommentVotesProps(initialVotes ?: emptyList()))
        }
    }
}
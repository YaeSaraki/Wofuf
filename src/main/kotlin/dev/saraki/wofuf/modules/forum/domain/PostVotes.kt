package dev.saraki.wofuf.modules.forum.domain

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/8 13:41
 *   @description:
 */
import dev.saraki.wofuf.shared.domain.WatchedList

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/6 13:05
 *   @description:
 */
data class PostVotesProps(
    val value: List<PostVote>
)

class PostVotes private constructor(props: PostVotesProps) :
    WatchedList<PostVote>(props.value) {

    override fun compareItems(a: PostVote, b: PostVote): Boolean {
        return a == b
    }

    companion object {
        fun create(initialVotes: List<PostVote>? = null): PostVotes {
            return PostVotes(PostVotesProps(initialVotes ?: emptyList()))
        }
    }
}
package dev.saraki.wofuf.modules.forum.domain.valueObjects

/**
 * 投票状态值对象
 * 用于表示用户对某个实体的投票状态
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 */
data class VoteStatus(
    val wasUpvotedByMe: Boolean,
    val wasDownvotedByMe: Boolean
) {
    companion object {
        fun empty() = VoteStatus(false, false)
    }
}

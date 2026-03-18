package dev.saraki.wofuf.modules.forum.domain.valueObjects

/**
 * 投票结果值对象
 * 用于表示投票操作后的结果
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 */
data class VoteResult(
    val newPoints: Int,
    val entityId: String
)

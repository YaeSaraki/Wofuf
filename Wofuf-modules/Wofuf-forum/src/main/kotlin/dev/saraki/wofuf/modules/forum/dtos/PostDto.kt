package dev.saraki.wofuf.modules.forum.dtos

import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostCategory
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostStatus
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostType
import java.time.LocalDateTime

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/7 16:00
 *   @description:
 */
data class PostDto(
    val postId: String,
    val slug: String,
    val title: String,
    val createdAt: LocalDateTime,
    val memberPostBy: MemberDto,
    val numComments: Int,
    val points: Int,
    val text: String,
    val link: String,
    val type: PostType,
    val category: PostCategory = PostCategory.DISCUSSION,
    val status: PostStatus = PostStatus.NORMAL,
    val isPinned: Boolean = false,
    val isFeatured: Boolean = false,
    val wasUpvotedByMe: Boolean?,
    val wasDownvotedByMe: Boolean?
)

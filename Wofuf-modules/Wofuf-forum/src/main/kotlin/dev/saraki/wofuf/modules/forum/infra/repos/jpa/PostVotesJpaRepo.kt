package dev.saraki.wofuf.modules.forum.infra.repos.jpa

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/9 16:32
 *   @description:
 */

import dev.saraki.wofuf.modules.forum.infra.repos.jpa.entities.PostVoteEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PostVotesJpaRepo : JpaRepository<PostVoteEntity, String> {
    fun existsByPostIdAndMemberIdAndVoteType(postId: String, memberId: String, voteType: String): Boolean

    fun findByPostIdAndMemberId(postId: String, memberId: String): List<PostVoteEntity>

    @Query("SELECT COUNT(pv) FROM PostVoteEntity pv WHERE pv.postId = :postId AND pv.voteType = :voteType")
    fun countByPostIdAndVoteType(@Param("postId") postId: String, @Param("voteType") voteType: String): Int
}
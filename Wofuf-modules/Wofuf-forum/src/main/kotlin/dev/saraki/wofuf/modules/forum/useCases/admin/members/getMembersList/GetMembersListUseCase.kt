package dev.saraki.wofuf.modules.forum.useCases.admin.members.getMembersList

import dev.saraki.wofuf.modules.forum.infra.annotation.RequirePermission
import dev.saraki.wofuf.modules.forum.infra.repos.MemberRepo
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PermissionPoint
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import org.springframework.stereotype.Service

@Service
class GetMembersListUseCase(
    private val memberRepo: MemberRepo,
) : UseCase<GetMembersListDto.Request, GetMembersListDto.Response> {

    @RequirePermission(PermissionPoint.VIEW_MEMBER_PROFILES, "Only users with VIEW_MEMBER_PROFILES permission can view member list")
    override fun execute(request: GetMembersListDto.Request): Result<GetMembersListDto.Response> {
        val members = if (request.nickname.isNullOrBlank()) {
            // 获取活跃成员列表
            memberRepo.findActiveMembers(request.page, request.size)
        } else {
            // 按昵称搜索
            memberRepo.findMembersByNickname(request.nickname, request.page, request.size)
        }

        val total = if (request.nickname.isNullOrBlank()) {
            memberRepo.countActiveMembers()
        } else {
            memberRepo.countMembersByNickname(request.nickname)
        }

        val memberSummaries = members.map { member ->
            GetMembersListDto.MemberSummary(
                memberId = member.memberId.stringValue,
                userId = member.userId.stringValue,
                playerId = member.playerId.stringValue,
                nickname = member.nickname.value,
                reputation = member.reputation,
                isBanned = member.isBanned,
                permissions = member.permissions.map { it.name }
            )
        }

        return Result.success(
            GetMembersListDto.Response(
                members = memberSummaries,
                total = total,
                page = request.page,
                size = request.size
            )
        )
    }
}

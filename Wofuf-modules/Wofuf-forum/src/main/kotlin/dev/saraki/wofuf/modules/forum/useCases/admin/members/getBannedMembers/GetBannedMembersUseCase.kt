package dev.saraki.wofuf.modules.forum.useCases.admin.members.getBannedMembers

import dev.saraki.wofuf.modules.forum.domain.valueObjects.PermissionPoint
import dev.saraki.wofuf.modules.forum.infra.annotation.RequirePermission
import dev.saraki.wofuf.modules.forum.infra.repos.MemberRepo
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import org.springframework.stereotype.Service
import java.time.ZoneOffset

@Service
class GetBannedMembersUseCase(
    private val memberRepo: MemberRepo,
) : UseCase<GetBannedMembersDto.Request, GetBannedMembersDto.Response> {

    @RequirePermission(PermissionPoint.USER_VIEW_BANNED, "Only users with USER_VIEW_BANNED permission can view banned members")
    override fun execute(request: GetBannedMembersDto.Request): Result<GetBannedMembersDto.Response> {
        val page = request.page.coerceAtLeast(0)
        val size = request.size.coerceIn(1, 100)

        val members = memberRepo.findBannedMembers(page, size)
        val total = memberRepo.countBannedMembers()

        val memberSummaries = members.map { member ->
            GetBannedMembersDto.MemberSummary(
                memberId = member.memberId.stringValue,
                nickname = member.nickname.value,
                isBanned = member.isBanned,
                bannedAt = member.bannedAt?.toEpochSecond(ZoneOffset.UTC),
                bannedUntil = member.bannedUntil?.toEpochSecond(ZoneOffset.UTC),
                bannedReason = member.bannedReason,
                bannedBy = member.bannedBy?.stringValue
            )
        }

        return Result.success(GetBannedMembersDto.Response(
            members = memberSummaries,
            total = total,
            page = page,
            size = size
        ))
    }
}

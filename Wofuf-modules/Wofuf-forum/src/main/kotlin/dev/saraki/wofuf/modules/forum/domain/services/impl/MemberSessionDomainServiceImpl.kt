package dev.saraki.wofuf.modules.forum.domain.services.impl

import dev.saraki.wofuf.modules.forum.domain.Member
import dev.saraki.wofuf.modules.forum.domain.events.MemberLoggedOut
import dev.saraki.wofuf.modules.forum.domain.services.MemberSessionDomainService
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
import dev.saraki.wofuf.modules.forum.infra.repos.MemberRepo
import dev.saraki.wofuf.shared.core.AppError
import dev.saraki.wofuf.shared.core.Result
import org.springframework.stereotype.Service

/**
 * 成员会话领域服务实现
 *
 * @author YaeSaraki
 * @date 2026/4/24
 */
@Service
class MemberSessionDomainServiceImpl(
    private val memberRepo: MemberRepo
) : MemberSessionDomainService {

    override fun handleLogout(memberId: MemberId, jti: String): Result<Member> {
        // 1. 查找成员
        val member = memberRepo.findMemberById(memberId)
            ?: return Result.failure(AppError("成员不存在"))

        // 2. 添加退出事件
        member.addLogoutEvent(jti)

        // 3. 保存成员（触发事件发布）
        val savedMember = memberRepo.save(member) ?: member

        return Result.success(savedMember)
    }
}